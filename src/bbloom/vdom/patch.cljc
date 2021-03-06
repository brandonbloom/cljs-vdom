(ns bbloom.vdom.patch
  (:refer-clojure :exclude [create-node]) ;XXX core leaking this private?
  (:require [clojure.set :as set]
            [bbloom.vdom.core :as vdom]
            [bbloom.vdom.trace :refer [traced]]))

(defn update-text [vdom before {:keys [id text] :as after}]
  (if (= (:text before) text)
    vdom
    (vdom/set-text vdom id text)))

(defn detach-last-child [vdom id]
  (vdom/detach vdom (-> (vdom/node vdom id) :children peek)))

(defn diff-maps [before after]
  (let [removed (set/difference (-> before keys set) (-> after keys set))]
    (reduce (fn [acc [k val]]
              (let [old (get before k)]
                (cond
                  (= old val) acc
                  (map? val) (let [sub (diff-maps old val)]
                               (if (seq sub)
                                 (assoc acc k sub)
                                 acc))
                  :else (assoc acc k val))))
            (when (seq removed)
              (into {} (map #(vector % nil)) removed))
            after)))

(defn update-element [vdom before {:keys [id props] :as after}]
  (let [updated (diff-maps (:props before) props)]
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
        vdom (transduce
               (map-indexed vector)
               (completing
                 (fn [vdom [i child]]
                   (assert (nil? (*parented* child))
                           (str "Duplicate node id: " child))
                   (set! *parented* (conj *parented* child))
                   (if (= (get-in (vdom/node vdom id) [:children i]) child)
                     vdom
                     (vdom/insert-child vdom id i child))))
               vdom
               children)
        ;; Detach any leftover trailing children.
        n (max 0 (- (count (:children (vdom/node vdom id)))
                    (count children)))
        vdom (nth (iterate (fn [vdom]
                             (detach-last-child vdom id))
                           vdom)
                  n)]
    vdom))

(defn patch [vdom goal]
  (let [N0 (vdom/nodes vdom), M0 (vdom/mounts vdom), H0 (vdom/hosts vdom)
        N1 (vdom/nodes goal), M1 (vdom/mounts goal), H1 (vdom/hosts goal)
        ;; Unmount.
        vdom (transduce (comp (remove (fn [[eid nid]] (= (M1 eid) nid)))
                              (map second))
                        (completing vdom/unmount)
                        vdom M0)
        ;; Patch.
        vdom (reduce patch-node vdom N1)
        vdom (binding [*parented* #{}]
               (transduce (filter #(-> % :tag string?))
                          (completing patch-children)
                          vdom N1))
        ;; Free.
        freed (set/difference (into #{} (map :id) N0) (into #{} (map :id) N1))
        vdom (transduce (remove #(:parent (vdom/node vdom %)))
                        (completing vdom/free)
                        vdom freed)
        ;; Mount.
        vdom (transduce (remove (fn [[nid eid]] (= (H0 nid) eid)))
                        (completing (fn [vdom [nid eid]]
                                      (vdom/mount vdom eid nid)))
                        vdom H1)]
    vdom))

(defn trace-patch [before after]
  (-> (traced before) (patch after) :trace))

(defn diff [before after]
  (map second (trace-patch before after)))
