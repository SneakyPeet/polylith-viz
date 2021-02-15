(ns poly-viz.search.hiccup
  (:require [cheshire.core :as json]))


(def ^:private elasticlunr-js "http://elasticlunr.com/elasticlunr.min.js")

(defn- flatten-definitions [search]
  (->> search
       (map :definitions)
       (reduce into)
       (map #(assoc % :id (str (:interface %) (:name %) (:type %))))
       (sort-by (juxt :interface :name))))


(def ^:private js "
var elIndex = elasticlunr(function () {
    this.addField('interface');
    this.addField('type');
    this.addField('name');
    this.setRef('id');
});

DOC_DATA.forEach(d => elIndex.addDoc(d));

function search(i) {
  return elIndex.search(i).map(x => x.doc);
}


")


(defn search-component [search]
  (let [data-json (json/generate-string (flatten-definitions search) {:escape-non-ascii true})]
    [:div
     [:div#docs
      "TODO"]
     [:script {:type "text/javascript" :src elasticlunr-js}]
     [:script
      "var DOC_DATA = " data-json  ";"
      js
      ]]))


#_{:name "article",
   :type "function",
   :parameters [{:name "auth-user"} {:name "slug"}],
   :interface "article"}
