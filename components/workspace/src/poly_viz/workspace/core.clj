(ns poly-viz.workspace.core
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]))


(defn from-path [path]
  (read-string (slurp path)))


(defn- definition-type-class [t]
  (let [c
        (get {"data" "is-warning"
              "function" "is-info"
              "macro" "is-danger"} t "")]
    [:span.tag.is-normal.is-light.mb-0 {:class c} t]))


(defn- enrich-definition [interface definition]
  (let [data (dissoc definition :name :type)]
    (assoc definition
           :interface (:name interface)
           :display-component
           [:div.block
            [:div.tags.has-addons.mb-0
             [:span.tag.has-text-weight-bold.mb-0 (:name definition)]
             [:span.tag.is-primary.is-light.mb-0 (:name interface)]
             (definition-type-class (:type definition))]
            (when-not (empty? data)
              [:pre.p-1.pl-4 (with-out-str
                               (pprint/pprint data))])])))


(defn enrich [ws]
  (update ws
          :interfaces
          (fn [is]
            (map (fn [i]
                   (update i :definitions
                           #(map (partial enrich-definition i) %))) is))))


(comment
  (->> (from-path "ws.edn")
       enrich
       :interfaces)
  )
