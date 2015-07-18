(ns bbloom.vdom.patch
  (:refer-clojure :exclude [create-node]) ;XXX core leaking this private?
  (:require [clojure.set :as set]
            [bbloom.vdom.core :as vdom]
            [bbloom.vdom.trace :refer [traced]]))

(defn update-text [vdom before {:keys [id text] :as after}]
  (if (= (:text before) text)
    vdom
    (vdom/set-text vdom id text)))

(defn detatch-last-child [vdom id]
  (vdom/detatch vdom (-> (vdom/node vdom id) :children peek)))

(defn update-element [vdom before {:keys [id props] :as after}]
  (let [removed (set/difference (-> before :props keys set)
                                (-> props keys set))
        updated (when (seq removed)
                  (into {} (for [prop removed] [prop nil])))
        old-props (:props before)
        updated (reduce (fn [acc [k val]]
                          (if (= (old-props k val) val)
                            acc
                            ;;XXX is value is map, recursively merge.
                            (assoc acc k val)))
                        updated
                        props)]
    (if (seq updated)
      (vdom/set-props vdom id updated)
      vdom)))

(defn update-node [vdom before {:keys [id tag] :as after}]
  (assert (= (:tag before) tag) (str "Cannot transmute node type for id " id))
  (if (= tag :text)
    (update-text vdom before after)
    (update-element vdom before after)))

(defn create-node [vdom {:keys [id tag] :as node}]
  (if (= tag :text)
    (vdom/create-text vdom id (:text node))
    (let [{:keys [props]} node
          vdom (vdom/create-element vdom id tag)]
      (if (seq props)
        (vdom/set-props vdom id (:props node))
        vdom))))

(defn patch-node [vdom {:keys [id tag] :as node}]
  (if-let [before (vdom/node vdom id)]
    (update-node vdom before node)
    (create-node vdom node)))

(def ^:dynamic *parented*) ;XXX debug-only

(defn patch-children [vdom {:keys [id children]}]
  (let [;; Move desired children in to place.
        vdom (reduce (fn [vdom [i child]]
                       (assert (nil? (*parented* child))
                               (str "Duplicate node id: " child))
                       (set! *parented* (conj *parented* child))
                       (if (= (get-in (vdom/node vdom id) [:children i]) child)
                         vdom
                         (vdom/insert-child vdom id i child)))
             vdom
             (map vector (range) children))
        ;; Detatch any leftover trailing children.
        n (max 0 (- (count (:children (vdom/node vdom id)))
                    (count children)))
        vdom (nth (iterate (fn [vdom]
                             (detatch-last-child vdom id))
                           vdom)
                  n)]
    vdom))

(defn patch [vdom goal]
  (let [N0 (vdom/nodes vdom), M0 (vdom/mounts vdom), H0 (vdom/hosts vdom)
        N1 (vdom/nodes goal), M1 (vdom/mounts goal), H1 (vdom/hosts goal)
        unmounted (->> (remove (fn [[eid nid]] (= (M1 eid) nid)) M0)
                       (map second))
        mounted (remove (fn [[nid eid]] (= (H0 nid) eid)) H1)
        freed (set/difference (set (map :id N0)) (set (map :id N1)))
        vdom (reduce vdom/unmount vdom unmounted)
        vdom (reduce patch-node vdom N1)
        els (filter #(-> % :tag string?) N1)
        vdom (binding [*parented* #{}]
               (reduce patch-children vdom els))
        freed (remove #(:parent (vdom/node vdom %)) freed)
        vdom (reduce vdom/free vdom freed)
        vdom (reduce (fn [vdom [nid eid]]
                          (vdom/mount vdom eid nid))
                        vdom
                        mounted)]
    vdom))

(defn diff [before after]
  (-> (traced before) (patch after) :trace))

(comment

  (defn assert-patch [before after]
    (let [after* (patch before after)]
      (fipp.edn/pprint
        (if (not= after* after)
          {:before before
           :expected after
           :actual after*}
          {:before before
           :after after}))))

  (defn party [before after]
    (assert-patch before after)
    (fipp.edn/pprint (diff before after))
    )

  (require '[bbloom.vdom.syntax :refer [seqs->vdom]])
  (require '[bbloom.vdom.trace :refer [traced]])
  (let [vdom (seqs->vdom '(div {"tabindex" 0}
                            (span {:key "k"} "foo")
                            (b {} "bar")))
        vdom (vdom/mount vdom "blah" [["div" 0]])
        ]
    ;(party vdom/null vdom)
    (party vdom vdom/null)
    )

)
