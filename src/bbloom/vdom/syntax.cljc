(ns bbloom.vdom.syntax
  "Parse an Edn tree in to a vdom graph."
  (:require [bbloom.vdom.core :as vdom]))

;;XXX The code in this file is only really used for debugging and
;;XXX should not be part of the public API. Move to test directory?

;;XXX Should this use vdom/create-element, etc rather than direct updates?
;;^^^ Nah, instead validate the result.

(declare seqs->maps)

(defn seq->map [[tag props & children]]
  {:tag (name tag) ;XXX ignores namespace (on purpose for syntax quote)
   :props props
   :children (mapv seqs->maps children)})

(defn seqs->maps [x]
  (cond
    (string? x) {:tag :text :text x}
    (seq? x) (seq->map x)
    :else (throw (ex-info "Invalid dom syntax" {:val x}))))

(defn assign-ids
  "Simple ID scheme of visual-tree paths. Each path element is a pair of
  the node type and either the index within the parent or an explicit key."
  ([x]
   (first (assign-ids [x] [])))
  ([xs path]
   (mapv (fn [i x]
           (let [tag (:tag x)
                 k (get-in x [:props :key] i)
                 id (conj path [tag k])
                 x (assoc x :id id)]
             (if (string? tag)
               (-> x
                   (update :props dissoc :key)
                   (update :children assign-ids id))
               x)))
         (range)
         xs)))

(defn maps->nodes
  ([x] (maps->nodes {} x))
  ([nodes {:keys [id children] :as x}]
   (assert (some? id) (str "No id for node: " x))
   (assert (nil? (nodes id)) (str "Duplicate ID: " id))
   (reduce maps->nodes
           (assoc nodes id
                  (if (-> x :tag string?)
                    (update x :children #(mapv :id %))
                    x))
           (map #(assoc % :parent id) children))))

(defn maps->vdom [x]
  (let [g (maps->nodes x)]
    (assoc vdom/null :nodes g :detached #{(:id x)})))

(defn seqs->vdom [x]
  (-> x seqs->maps assign-ids maps->vdom))

(comment

  (require '[bbloom.vdom.core :as vdom])

  (-> '(div {"tabindex" 0}
         (span {:key "k"} "foo")
         (b {} "bar"))
      seqs->maps
      assign-ids
      maps->vdom
      (vdom/mount "root" [["div" 0]])
      ;(mount "root" [0 "k"])
      fipp.edn/pprint
      )

)
