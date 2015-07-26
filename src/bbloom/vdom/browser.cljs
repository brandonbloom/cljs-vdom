(ns bbloom.vdom.browser
  (:require [bbloom.vdom.core :as vdom]
            [bbloom.vdom.patch :refer [trace-patch]]))

(defonce global (atom {:vdom vdom/null :node->id {} :id->node {}}))

(defmulti mutate (fn [state [method & args]] method))

(defn render [vdom]
  (let [state @global
        trace (trace-patch (:vdom state) vdom)
        state (reduce (fn [state [vdom op]]
                        (mutate (assoc state :vdom vdom) op))
                      (assoc state :vdom vdom :created [] :destroyed [])
                      trace)]
    (reset! global (dissoc state :created :destroyed))
    (-> state
        (select-keys [:created :destroyed])
        (assoc :trace trace))))

(defn lookup [id]
  (get-in @global [:id->node id]))

(defn identify [node]
  (get-in @global [:node->id node]))

(defmethod mutate :mount [{:keys [id->node] :as state} [_ eid id]]
  (let [el (.getElementById js/document eid)]
    (assert el (str "No element with id: " eid))
    (.appendChild el (id->node id))
    state))

(defn- detach [{:keys [id->node] :as state} id]
  (let [child (id->node id)]
    (.removeChild (.-parentNode child) child))
  state)

(defmethod mutate :unmount [state [_ id]]
  (detach state id))

(defmethod mutate :detach [state [_ id]]
  (detach state id))

(defn create [state id node]
  (-> state
      (update :created conj [id node])
      (assoc-in [:id->node id] node)
      (assoc-in [:node->id node] id)))

(defmethod mutate :create-text [state [_ id text]]
  (create state id (.createTextNode js/document text)))

(defmethod mutate :set-text [{:keys [id->node] :as state} [_ id text]]
  (set! (.-nodeValue (id->node id)) text)
  state)

(defmethod mutate :create-element [state [_ id tag]]
  (create state id (.createElement js/document tag)))

(defmethod mutate :set-props [{:keys [id->node] :as state} [_ id props]]
  (let [node (id->node id)]
    (doseq [[k v] (dissoc props "attributes" "style")]
      ;;XXX for nil values, virtual-dom sets to "" if prev was a string. Why?
      ;;^^^ If this is necessary, can it be done during diff?
      (aset node k v))
    (doseq [[k v] (props "attributes")]
      (if (nil? v)
        (.removeAttribute node k)
        (.setAttribute node k v)))
    (doseq [[k v] (props "style")]
      (aset node "style" k (if (nil? v) "" v))))
  state)

(defmethod mutate :insert-child
  [{:keys [id->node] :as state} [_ parent-id index child-id]]
  (let [parent (id->node parent-id)
        siblings (.-children parent)
        child (id->node child-id)]
    (if (= (alength siblings) index)
      (.appendChild parent child)
      (let [sibling (aget siblings index)]
        (.insertBefore parent child sibling)))
    state))

(defmethod mutate :free [{:keys [vdom id->node] :as state} [_ id]]
  ((fn rec [state id]
     (let [node (id->node id)]
       (reduce rec
               (-> state
                   (update :destroyed conj [id node])
                   (update-in [:id->node] dissoc id)
                   (update-in [:node->id] dissoc node))
               (:children (vdom/node vdom id)))))
   state id))
