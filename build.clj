(require 'cljs.build.api)

(cljs.build.api/build "src"
  {:main 'bbloom.vdom.playground
   :output-to "out/main.js"
   :verbose true})
