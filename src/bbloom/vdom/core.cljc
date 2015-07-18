(ns bbloom.vdom.core
  (:require [bbloom.vdom.util :as util]))

(defprotocol IDom
  "Models multiple DOM trees relationally (ie. linked by IDs). The root of
  each tree can be either 'mounted' on to an external DOM or 'detatched'.
  Provides a minimal set of atomic manipulations which mirror efficient
  mutations of a browser DOM."
  ;; Accessors
  (node [vdom id])
  (nodes [vdom])
  (mounts [vdom])
  (hosts [vdom])
  ;; Manipulations
  (mount [vdom eid id])
  (unmount [vdom id])
  (detatch [vdom id])
  (create-text [vdom id text])
  (set-text [vdom id text])
  (create-element [vdom id tag])
  (set-props [vdom id props])
  (insert-child [vdom parent-id index child-id])
  (free [vdom id])
  )

;;TODO :parent is stored on nodes directly, should it be a top-level map?
(defrecord VDom [nodes mounts hosts detatched]

  IDom

  ;; Accessors

  (node [vdom id]
    (get-in vdom [:nodes id]))

  (nodes [vdom]
    (-> vdom :nodes vals))

  (mounts [vdom]
    (:mounts vdom))

  (hosts [vdom]
    (:hosts vdom))

  ;; Manipulations

  (mount [vdom eid id]
    (let [n (get-in vdom [:nodes id])]
      (assert n (str "Cannot mount unknown node: " id))
      (assert (nil? (:parent n)) (str "Cannot mount interior node: " id)))
    (assert (nil? (get-in vdom [:hosts id])) (str "Already mounted: " id))
    (-> vdom
        (assoc-in [:mounts eid] id)
        (assoc-in [:hosts id] eid)
        (update :detatched disj id)))

  (unmount [vdom id]
    (let [n (get-in vdom [:nodes id])]
      (assert n (str "Cannot unmount unknown node: " id))
      (assert (get-in vdom [:hosts id]) (str "Node already not mounted: " id))
      (-> vdom
          (update :mounts dissoc id)
          (update :hosts dissoc id)
          (update :detatched conj id))))

  (detatch [vdom id]
    (let [{:keys [parent] :as n} (get-in vdom [:nodes id])]
      (assert n (str "No such node id: " id))
      (assert parent (str "No already detatched: " id))
      (-> vdom
          (update-in [:nodes parent :children] util/remove-item id)
          (update-in [:nodes id] dissoc :parent)
          (update :detatched conj id))))

  (create-text [vdom id text]
    (assert (nil? (get-in vdom [:nodes id])) (str "Node already exists: " id))
    (-> vdom
        (assoc-in [:nodes id] {:id id :tag :text :text text})
        (update :detatched conj id)))

  (set-text [vdom id text]
    (assert (= (get-in vdom [:nodes id :tag]) :text)
            (str "Cannot set text of non-text node: " id))
    (assoc-in vdom [:nodes id :text] text))

  (create-element [vdom id tag]
    (assert (nil? (get-in vdom [:nodes id])) (str "Node already exists: " id))
    (-> vdom
        (assoc-in [:nodes id] {:id id :tag tag :children []})
        (update :detatched conj id)))

  (set-props [vdom id props]
    (assert (string? (get-in vdom [:nodes id :tag]))
            (str "Cannot set props of non-element node: " id))
    ;;XXX recursively merge map values & dissoc when values are nil.
    (update-in vdom [:nodes id :props] merge props))

  (insert-child [vdom parent-id index child-id]
    ;;TODO This needs an assert or two.
    (let [n (get-in vdom [:nodes child-id])
          vdom (if-let [p (:parent n)]
                 (update-in vdom [:nodes p :children] remove-item child-id)
                 vdom)]
      (-> vdom
          (assoc-in [:nodes child-id :parent] parent-id)
          (update-in [:nodes parent-id :children] util/insert index child-id)
          (update-in [:detatched] disj child-id))))

  (free [vdom id]
    (assert (get-in vdom [:detatched id])
            (str "Cannot free non-detatched node:" id))
    ((fn rec [vdom id]
       (reduce rec
               (update vdom :nodes dissoc id)
               (get-in vdom [:nodes id :children])))
     (update vdom :detatched disj id) id))

  )

(def null (map->VDom {:nodes {} :mounts {} :hosts {} :detatched #{}}))
