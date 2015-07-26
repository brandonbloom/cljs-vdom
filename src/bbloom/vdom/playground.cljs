(ns bbloom.vdom.playground
  (:require [cljs.pprint :refer [pprint]]
            [clojure.browser.repl :as repl]
            [bbloom.vdom.core :as vdom]
            [bbloom.vdom.browser :refer [render]]
            [bbloom.vdom.syntax :refer [seqs->vdom]] ;XXX
            ))

(defonce conn
  (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(println "Hello world!")

;; defonce because functions have reference equality.
(defonce onclick
  (fn [e]
    (.log js/console "onclick" e)))

(def tree
  `(div {"tabindex" 0}
     (i {:key "c"} "italic!")
     (span {:key "a"} "foo")
     (b {} #_(i {} "bar"))
     (div {:key "b"
           "style" {"color" "red"}} "foox")
     (input {:key "c"
             "value" "abx"})
     (button {"onclick" ~onclick}
       "click me")
     ))

(-> tree
    seqs->vdom
    (vdom/mount "root" [["div" 0]])
    render
    (select-keys [#_:trace :created :destroyed])
    pprint)
