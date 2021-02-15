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

function result(r){
  return '<div class=\"block\">'+ r.interface + '/' + r.name + ' - ' + r.type + '</div>';
}

function showResults(results) {
  var el = document.getElementById('search-results');
  if(results.length === 0) {
    el.innerHTML = '<p>No Results</p>'
  } else {
    el.innerHTML = results.map(result).join('');
  }

}

function search(i) {
  return elIndex.search(i).map(x => x.doc);
}

function handleSearch(e) {
  e.preventDefault();
  var value = document.getElementById('search-input').value;
  showResults(search(value));
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


(defn search-component [search]
  (let [data-json (json/generate-string (flatten-definitions search) {:escape-non-ascii true})]
    [:div
     search-box
     [:div#search-results]
     [:script {:type "text/javascript" :src elasticlunr-js}]
     [:script
      "var DOC_DATA = " data-json  ";"
      js
      ]]))


#_{:name "article",
   :type "function",
   :parameters [{:name "auth-user"} {:name "slug"}],
   :interface "article"}
