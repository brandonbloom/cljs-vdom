(ns bbloom.vdom.core-test
  (:use bbloom.vdom.core))

(-> null
    (create-element :x "div")
    (set-props :x {"a" 1})
    (set-props :x {"b" 2})
    ;(set-props :x {"a" nil})
    (set-props :x {"nest" {"x" 3}})
    ;(set-props :x {"nest" {"x" nil}})
    fipp.edn/pprint
    )
