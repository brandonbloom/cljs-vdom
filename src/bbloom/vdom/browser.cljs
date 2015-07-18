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

(defn- detatch [nodes id]
  (let [child (nodes id)]
    (.removeChild (.-parentNode child) child))
  nodes)

(defmethod mutate :unmount [nodes [_ id]]
  (detatch nodes id))

(defmethod mutate :detatch [nodes [_ id]]
  (detatch nodes id))

(defmethod mutate :create-text [nodes [_ id text]]
  (assoc nodes id (.createTextNode js/document text)))

(defmethod mutate :set-text [nodes [_ id text]]
  (set! (.-nodeValue (nodes id)) text)
  nodes)

(defmethod mutate :create-element [nodes [_ id tag]]
  (assoc nodes id (.createElement js/document tag)))

(defmethod mutate :remove-props [nodes [_ id props]]
  ;;XXX handle recursive maps specially:
  ;;XXX clearing style sub-props: set to ''   -- necessary?
  ;;XXX clearing attributes sub-props: call removeAttribute
  nodes)

(defmethod mutate :set-props [nodes [_ id props]]
  ;;XXX handle recursive maps specially
  ;;XXX set nested for style sub-props
  ;;XXX call setAttribute for attributes sub-props
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
