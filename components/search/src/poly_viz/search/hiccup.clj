(ns poly-viz.search.hiccup
  (:require [cheshire.core :as json]
            [hiccup.core :as hiccup]))


(def ^:private elasticlunr-js "http://elasticlunr.com/elasticlunr.min.js")

(defn- prep-definition [definition]
  (-> definition
      (assoc :id (str (:interface definition) (:name definition) (:type definition)))
      (update :display-component #(hiccup/html %))))


(defn- flatten-definitions [enriched-ws]
  (->> (:interfaces enriched-ws)
       (map :definitions)
       (reduce into)
       (map prep-definition)
       (sort-by (juxt :interface :name))))


(def ^:private js "
var elIndex = elasticlunr(function () {
    this.addField('interface');
    this.addField('type');
    this.addField('name');
    this.setRef('id');
});

DOC_DATA.forEach(d => elIndex.addDoc(d));

function handleSearch(e) {
  e.preventDefault();
  var value = document.getElementById('search-input').value;
  var results = elIndex.search(value,{
    fields: {
        name: {
            boost: 3,
            expand: true
        },
        interface: {boost: 2},
        type: {boost: 1}
    },
    bool: 'OR',
    expand: false
  }).map(x => x.doc);
  var el = document.getElementById('search-results');
  el.innerHTML = results.length === 0
    ? '<p>No Results</p>'
    : results.map(x => x['display-component']).join('');
}

document.getElementById('search-form').addEventListener('submit', handleSearch)
")


(def ^:private search-box
  [:div.block
   [:form#search-form
    [:div.field.has-addons
     [:div.control
      [:input#search-input.input {:type "text"}]]
     [:div.control
      [:button.button.is-info
       {:type "submit"}
       "Search"]]]]])


(defn search-component [enriched-ws]
  (let [data-json (json/generate-string (flatten-definitions enriched-ws) {:escape-non-ascii true})]
    [:div
     search-box
     [:div#search-results]
     [:script {:type "text/javascript" :src elasticlunr-js}]
     [:script
      "var DOC_DATA = " data-json  ";"
      js]]))
