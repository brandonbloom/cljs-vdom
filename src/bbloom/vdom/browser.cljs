(ns bbloom.vdom.browser
  (:require [bbloom.vdom.core :as vdom]
            [bbloom.vdom.patch :refer [diff]]))

(defonce state (atom {:vdom vdom/null :nodes {}}))

(defmulti mutate (fn [nodes [method & args]] method))

(defmethod mutate :mount [nodes [_ eid id]]
  (let [el (.getElementById js/document eid)]
    (assert el (str "No element with id: " eid))
    (.appendChild el (nodes id))
    nodes))

(defn- detach [nodes id]
  (let [child (nodes id)]
    (.removeChild (.-parentNode child) child))
  nodes)

(defmethod mutate :unmount [nodes [_ id]]
  (detach nodes id))

(defmethod mutate :detach [nodes [_ id]]
  (detach nodes id))

(defmethod mutate :create-text [nodes [_ id text]]
  (assoc nodes id (.createTextNode js/document text)))

(defmethod mutate :set-text [nodes [_ id text]]
  (set! (.-nodeValue (nodes id)) text)
  nodes)

(defmethod mutate :create-element [nodes [_ id tag]]
  (assoc nodes id (.createElement js/document tag)))

(defmethod mutate :set-props [nodes [_ id props]]
  (let [node (nodes id)]
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
  nodes)

(defmethod mutate :insert-child [nodes [_ parent-id index child-id]]
  (let [parent (nodes parent-id)
        siblings (.-children parent)
        child (nodes child-id)]
    (if (= (alength siblings) index)
      (.appendChild parent child)
      (let [sibling (aget siblings index)]
        (.insertBefore parent child sibling)))
    nodes))

(defmethod mutate :free [nodes [_ id]]
  nodes ;;XXX call dispose top-down, remove nodes from map
  )

(defn render [vdom]
  (swap! state (fn [state]
                 (cljs.pprint/pprint (diff (:vdom state) vdom))
                 {:vdom vdom
                  :nodes (reduce mutate
                                 (:nodes state)
                                 (diff (:vdom state) vdom))})))
