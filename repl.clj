(load-file "build.clj")

(require 'cljs.repl)
(require 'cljs.repl.browser)

(cljs.repl/repl (cljs.repl.browser/repl-env)
  :watch "src"
  :output-dir "target/out")
