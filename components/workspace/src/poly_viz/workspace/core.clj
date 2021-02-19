(ns poly-viz.workspace.core
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [codox.reader.clojure :as codox]
            [clojure.string :as string]))


(defn from-path [path]
  (read-string (slurp path)))


(defn- ns-str->path
  [ns]
  (-> ns
      (string/replace #"\." "/")
      (string/replace #"-" "_")))


(defn- enrich-interface-namespaces[ws]
  (let [{:keys [interface-ns top-namespace]} (:settings ws)]
    (update ws
            :interfaces
            (fn [interfaces]
              (->> interfaces
                   (map (fn [{:keys [name definitions] :as i}]
                          (let [component-ns-str (str top-namespace "." name)
                                component-path (str "components/" (ns-str->path name) "/src")
                                interface-ns-str (str component-ns-str "." interface-ns)
                                interface-ns (symbol interface-ns-str)
                                definitions (->> definitions
                                                 (map (fn [{:keys [sub-ns] :as d}]
                                                        (assoc d :namespace
                                                               (symbol
                                                                (if sub-ns
                                                                  (symbol (str interface-ns-str "." sub-ns))
                                                                  interface-ns))))))]
                            (assoc i :definitions definitions
                                   :namespace interface-ns
                                   :namespaces (set (conj (map :namespace definitions) interface-ns))
                                   :component-ns (symbol component-ns-str)
                                   :component-path component-path)))))))))


(defn- definition-type [{:keys [type arglist]}]
  (cond
    (and (= :var type) arglist) "function"
    (= :var type) "data"
    :else (name type))
  )


(defn- depricated-tag [doc]
  (when-let [deprecated (:deprecated doc)]
    [:div.tag.is-danger.is-light.mb-0
     "deprecated "
     (when-not (boolean? deprecated)
       (str deprecated))]))


(defn- enrich-doc [interface doc]
  (let [interface-data (dissoc doc :name :publics :doc :deprecated)]
    (-> doc
        (assoc :display-component
               [:div.block
                [:div.tags.has-addons.mb-0
                 [:span.tag.has-text-weight-bold.mb-0  (str (:name doc))]
                 [:div.tag.mb-0.is-info.is-light "ns"]
                 (depricated-tag doc)]
                (when-let [doc-str (:doc doc)]
                  [:p.notification.mb-0.p-1.pl-2.is-size-7
                   doc-str])
                (when-not (empty? interface-data)
                  [:pre.p-1.pl-2.is-size-7
                   (with-out-str
                     (pprint/pprint interface-data))])])
        (update :publics
                (fn [pubs]
                  (->> pubs
                       (map
                        (fn [definition]
                          (let [def-data (dissoc definition :name :file :arglists :line :doc :type :deprecated :doc/format)]
                            (assoc definition
                                   :interface (:name interface)
                                   :display-component
                                   [:div.block
                                    [:div.tags.has-addons.mb-0
                                     [:span.tag.has-text-weight-bold.mb-0 (str (:name definition))]
                                     [:span.tag.is-warning.is-light.mb-0
                                      (definition-type definition)]
                                     (depricated-tag definition)
                                     [:span.tag.is-info.is-light.mb-0 (:name interface)]
                                     #_ [:span (:file definition)]]
                                    (when-let [doc-str (:doc definition)]
                                      [:pre.notification.mb-0.p-0.pl-2.is-size-7
                                       doc-str])
                                    (when-let [arglists (:arglists definition)]
                                      [:pre.notification.mb-0.p-0.pl-2.is-size-7
                                       (string/join "\r\n" (map str arglists))])
                                    (when-not (empty? def-data)
                                      [:pre.p-0.pl-2.is-size-7 (with-out-str
                                                       (pprint/pprint def-data))])]))))))))))


(defn- enrich-interface-docs [ws]
  (let [interfaces (:interfaces ws)
        paths (map :component-path interfaces)
        interfaces-ns (set (mapcat :namespaces interfaces))
        docs (->> (codox/read-namespaces paths)
                  (map (juxt :name identity))
                  (into {}))]
    (update ws :interfaces
            (fn [ints]
              (->> ints
                   (map (fn [i]
                          (assoc i :docs (->> (:namespaces i)
                                              (map #(get docs %))
                                              (remove nil?)
                                              (map #(enrich-doc i %)))))))))))





"NOTES

:top level -> name doc publics

:var = fn or data

always avail
:name <symbol>
:file <can we open a file at a line?>
:line int
:type <show if this is not :var>

sometimes availalbe
:doc
:argslist <list of vecs of symbols>


:types
  :var :multimethod :macro
   :protocol -> has :members which follow the above

"


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


(defn- enrich-definitions [ws]
  (update ws :interfaces
          (fn [is]
            (map (fn [i]
                   (update i :definitions
                           #(map (partial enrich-definition i) %))) is))))


(defn enrich [ws]
  (-> ws
      enrich-interface-namespaces
      enrich-interface-docs
      enrich-definitions))



(comment

  (enrich (from-path "ws.edn"))

  (spit "docs-example.edn" (with-out-str (clojure.pprint/pprint (codox/read-namespaces ["components/example/src/poly_viz/example"]))))
)
