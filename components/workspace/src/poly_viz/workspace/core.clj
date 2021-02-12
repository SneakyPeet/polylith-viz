(ns poly-viz.workspace.core
  (:require [clojure.java.io :as io]))


(defn from-path [path]
  (read-string (slurp path)))


(comment
  (from-path "ws.edn")
  )
