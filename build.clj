(require 'cljs.build.api)

(cljs.build.api/build "src"
  {:main 'bbloom.vdom.playground
   :output-dir "target/out"
   :output-to "target/out/main.js"
   :verbose true})
