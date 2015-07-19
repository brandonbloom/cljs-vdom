(ns bbloom.vdom.trace
  (:require [bbloom.vdom.core :as vdom]))

;; The redundancy in this file is mildly annoying, but
;; my naive attempt to macro-ize it was majorly ugly.

(defrecord DomTrace [trace dom]

  vdom/IDom

  ;; Accessors (passthrough to underlying vdom).
  (node [vdom id] (vdom/node dom id))
  (nodes [vdom] (vdom/nodes dom))
  (mounts [vdom] (vdom/mounts dom))
  (hosts [vdom] (vdom/hosts dom))

  ;; Manipulations (add [vdom op] to trace before passthrough).

  (mount [vdom eid id]
    (DomTrace. (conj trace [dom [:mount eid id]])
               (vdom/mount dom eid id)))

  (unmount [vdom id]
    (DomTrace. (conj trace [dom [:unmount id]])
               (vdom/unmount dom id)))

  (detach [vdom id]
    (DomTrace. (conj trace [dom [:detach id]])
               (vdom/detach dom id)))

  (create-text [vdom id text]
    (DomTrace. (conj trace [dom [:create-text id text]])
               (vdom/create-text dom id text)))

  (set-text [vdom id text]
    (DomTrace. (conj trace [dom [:set-text id text]])
               (vdom/set-text dom id text)))

  (create-element [vdom id tag]
    (DomTrace. (conj trace [dom [:create-element id tag]])
               (vdom/create-element dom id tag)))

  (set-props [vdom id props]
    (DomTrace. (conj trace [dom [:set-props id props]])
               (vdom/set-props dom id props)))

  (insert-child [vdom parent-id index child-id]
    (DomTrace. (conj trace [dom [:insert-child parent-id index child-id]])
               (vdom/insert-child dom parent-id index child-id)))

  (free [vdom id]
    (DomTrace. (conj trace [dom [:free id]])
               (vdom/free dom id)))

  )

(defn traced [vdom]
  (DomTrace. [] vdom))
