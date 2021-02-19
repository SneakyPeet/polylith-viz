(ns poly-viz.example.interface.example2)

(defn foo2 [x y & z])

(defmacro bar2 [x & body])

(def baz2 true)

(def ^{:doc "Var with removed source link."} quz2 10)


(defn typed-f [^String s])
