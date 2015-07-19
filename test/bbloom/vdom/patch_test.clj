(ns bbloom.vdom.patch-test
  (:require [bbloom.vdom.core :as vdom]
            [bbloom.vdom.syntax :refer [seqs->vdom]]
            [bbloom.vdom.trace :refer [traced]])
  (:use bbloom.vdom.patch))

(defn assert-patch [before after]
  (let [after* (patch before after)]
    (fipp.edn/pprint
      (if (not= after* after)
        {:before before
         :expected after
         :actual after*}
        {:before before
         :after after}))))

(defn party [before after]
  (assert-patch before after)
  (fipp.edn/pprint (diff before after))
  )

(comment

(let [vdom (seqs->vdom '(div {"tabindex" 0}
                          (span {:key "k"} "foo")
                          (b {} "bar")))
      ;vdom (vdom/mount vdom "blah" [["div" 0]])
      ]
  ;(party vdom/null vdom)
  (party vdom vdom/null)
  )

(let [vdom (-> vdom/null
               (vdom/create-element :x "div")
               (vdom/set-props :x {"tabindex" 1 "style" {"color" "red"}}))]
    ;(party vdom (vdom/set-props vdom :x {"tabindex" 2}))
    (party vdom (vdom/set-props vdom :x {"style" {"background" "green"}}))
    )

)
