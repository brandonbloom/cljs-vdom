(ns bbloom.vdom.util
  (:require [clojure.core.rrb-vector :as rrb]))

(defn index-of [v x]
  (let [n (count v)]
    (loop [i 0]
      (cond
        (= i n) nil
        (= (nth v i) x) i
        :else (recur (inc i))))))

(defn remove-at [v i]
  (rrb/catvec (rrb/subvec v 0 i) (rrb/subvec v (inc i))))

(defn remove-item [v x]
  (remove-at v (index-of v x)))

(defn insert [v i x]
  (rrb/catvec (rrb/subvec v 0 i) [x] (rrb/subvec v i)))
