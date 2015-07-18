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

(render (vdom/mount (seqs->vdom '(div {"tabindex" 0}
                                   (i {:key "c"} "italic!")
                                   (span {:key "a"} "foo")
                                   (b {} "bar")
                                   (div {:key "b"
                                         "style" {"color" "red"}} "foox")
                                   (input {:key "c"
                                           "value" "abx"})
                                   ))
                    "root" [["div" 0]]))
