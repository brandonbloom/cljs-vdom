(ns bbloom.vdom.trace
  (:require [bbloom.vdom.core :as vdom]))

(comment

;;TODO macros to generate DomTrace methods.

(doseq [[kw {:keys [name arglists]}] (:sigs vdom/IDom)]
  (prn `(~name ~@(for [arglist arglists]
                   (list arglist
                         (list 'DomTrace.
                               (list `conj 'trace (into [kw] (next arglist)))
                               (list* (symbol "vdom" (str name))
                                      'dom
                                      (next arglist))))))))

)

(defrecord DomTrace [trace dom]

  vdom/IDom

  ;; Accessors
  (node [vdom id] (vdom/node dom id))
  (nodes [vdom] (vdom/nodes dom))
  (mounts [vdom] (vdom/mounts dom))
  (hosts [vdom] (vdom/hosts dom))

  ;; Manipulations

  (mount [vdom eid id]
    (DomTrace. (conj trace [:mount eid id])
               (vdom/mount dom eid id)))

  (unmount [vdom id]
    (DomTrace. (conj trace [:unmount id])
               (vdom/unmount dom id)))

  (detatch [vdom id]
    (DomTrace. (conj trace [:detatch id])
               (vdom/detatch dom id)))

  (create-text [vdom id text]
    (DomTrace. (conj trace [:create-text id text])
               (vdom/create-text dom id text)))

  (set-text [vdom id text]
    (DomTrace. (conj trace [:set-text id text])
               (vdom/set-text dom id text)))

  (create-element [vdom id tag]
    (DomTrace. (conj trace [:create-element id tag])
               (vdom/create-element dom id tag)))

  (set-props [vdom id props]
    (DomTrace. (conj trace [:set-props id props])
               (vdom/set-props dom id props)))

  (insert-child [vdom parent-id index child-id]
    (DomTrace. (conj trace [:insert-child parent-id index child-id])
               (vdom/insert-child dom parent-id index child-id)))

  (free [vdom id]
    (DomTrace. (conj trace [:free id])
               (vdom/free dom id)))

  )

(defn traced [vdom]
  (DomTrace. [] vdom))
