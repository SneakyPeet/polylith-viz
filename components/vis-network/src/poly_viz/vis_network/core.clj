(ns poly-viz.vis-network.core)


(defn- brick->id [brick]
  (str (:name brick) "-" (name (:type brick))))


(defn- extract-src-names
  "this exists for backwards compatibility"
  [k brick]
  (let [deps (get brick k)]
    (cond
      (map? deps)    (:src deps [])
      (vector? deps) deps
      :else          [])))


(defn- extract-interface-deps
  [brick]
  (extract-src-names :interface-deps brick))


(defn- extract-base-names
  [brick]
  (extract-src-names :base-names brick))


(defn- get-reversed-levels [components]
  (let [component-deps (->> components
                            (map (juxt :name extract-interface-deps))
                            (sort-by #(-> % last count))
                            vec)]
    (loop [component-deps component-deps
           result         {}]
      (if (empty? component-deps)
        result
        (let [head             (first component-deps)
              [component deps] head
              tail             (vec (rest component-deps))
              deps-levels      (map #(get result %) deps)
              can-calculate?   (empty? (filter nil? deps-levels))]
          (if-not can-calculate?
            (recur
              (conj tail head)
              result)
            (let [level (if (empty? deps-levels)
                          0
                          (inc (apply max deps-levels)))]
              (recur
                tail
                (assoc result component level)))))))))


(defn- components-depth [components]
  (let [reversed-levels (get-reversed-levels components)
        depth           (if (empty? reversed-levels)
                0
                (apply max (vals reversed-levels)))]
    (->> reversed-levels
         (map (fn [[k v]]
                [k (- depth v)]))
         (into {}))))

#_(def c (atom nil))


(defn dependecy-branches [components]
  (let [components (->> components
                        (map (juxt :name extract-interface-deps))
                        (sort-by #(count (second %)))
                        vec)]
    (loop [components components
           result     {}
           n          0]
      (let [component       (first components)
            [c-name c-deps] component
            tail            (vec (rest components))]

        (cond
          (empty? components)
          (->> result
               vals
               (reduce into)
               set
               (map reverse)
               (sort-by (juxt first count)))

          (empty? c-deps)
          (recur tail (assoc result c-name [[c-name]]) n)

          (>= n 100)
          (throw (ex-info "Limit" {:n               n
                                   :result          result
                                   :remaining-comps components}))

          (empty? (filter #(not (contains? result %)) c-deps))
          (recur tail (reduce
                        (fn [r' [k v]]
                          (update r' k into v))
                        result
                        (->> c-deps
                             (map (fn [dep]
                                    [c-name (->> (get result dep)
                                                 (map #(conj % c-name)))]))))
                 n)

          :else
          (recur (conj tail component) result (inc n)))))))

(defn components-depth-2 [components]
  (let [branches (dependecy-branches components)]
    (->> branches
         (map #(->>
                 (interleave % (range (count %)))
                 (partition 2)))
         (reduce into)
         (group-by first)
         (map (fn [[k v]]
                [k (->> v
                        (map last)
                        (apply max))]))
         (into {}))))

(defn components-depth-3 [components]
  (let [branches (dependecy-branches components)
        lookup   (->> branches
                    (map #(->>
                            (interleave % (range (count %)))
                            (partition 2)))
                    (reduce into)
                    (group-by first)
                    (map (fn [[k v]]
                           [k (->> v
                                   (map last)
                                   (apply max))]))
                    (into {}))]
    (->> branches
         (map (fn [deps]
                (->> deps
                     (map (fn [d]
                            [d (get lookup d)])))))
         (group-by (comp first first))
         (map (fn [[c deps]]
                (let [n  (->> deps
                             (map (comp second second))
                             (remove nil?))
                      n' (get lookup c)]
                  [c
                   ;;n'
                   (if (empty? n)
                     (get lookup c)
                     (max n' (dec (apply min n))))])))
         (into {}))))


(defn- ->node [brick-options brick-levels component-depth-lookup {:keys [name type] :as brick}]
  (let [type (keyword type)
        opts (get-in brick-options [type :nodes] {})
        id (brick->id brick)
        level (if (= :component type)
                (get component-depth-lookup name 0)
                0)]
    (assoc opts
           :label name
           :id id
           :level (+ (get brick-levels type 0) level))))

(defn- ws->nodes
  [brick-options brick-levels {:keys [projects bases components]}]
  #_(reset! c components)
  (let [component-depth-lookup (components-depth-3 components)]
    (->>
      [components
       bases
       projects]
      (reduce into)
      (map (partial ->node brick-options brick-levels component-depth-lookup)))))


(defn- brick->edges [opts deps-extract-fn brick-type brick]
  (let [from (brick->id brick)
        tos (or (deps-extract-fn brick) [])]
    (map #(assoc opts :from from :to (str % "-" (name brick-type))) tos)))


(defn- ws->edges
  [brick-options {:keys [projects bases components]}]
  (let [->edges (fn [t e-fn brick-type]
                  (fn [brick]
                    (brick->edges (get-in brick-options [t :edges] {}) e-fn brick-type brick)))]
    (->>
     [(map (->edges :component extract-interface-deps :component) components)
      (map (->edges :base extract-interface-deps :component) bases)
      (map (->edges :project extract-base-names :base) projects)]
     (reduce into)
     (reduce into))))


(defn ws->network
  [ws {:keys [include-dev? brick-options brick-levels]}]
  (let [ws (update ws :projects (fn [projects]
                                  (cond->> projects
                                    (false? include-dev?) (filter #(false? (:is-dev %))))))

        edges (ws->edges brick-options ws)]
    {:nodes (ws->nodes brick-options brick-levels ws)
     :edges edges}
    ))
