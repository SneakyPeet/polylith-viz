(ns poly-viz.vis-network.options)


(def default-vis-options
  {:layout {:improvedLayout true
            :hierarchical {:enabled          true
                           :direction        "UD"
                           :sortMethod       "directed"
                           :shakeTowards     "leaves"
                           :edgeMinimization true
                           :levelSeparation  100}}
   :edges  {:arrows {:to {:enabled     true
                          :type        "arrow"
                          :scaleFactor 0.8}}
            :color {:highlight "green"}
            :smooth {:enabled true
                     :type "cubicBezier"
                     :roundness 0.75
                     :forceDirection "vertical"}}
   :physics {:hierarchicalRepulsion {:avoidOverlap 1}}
   :nodes {:shape "box"
           :margin {:top 10 :bottom 10 :left 20 :right 20}}
   })


(def default-brick-vis-options
  {:component {:nodes {:color "#ED553B"}
               :edges {:color {:color "#ED553B"}}}
   :base {:nodes {:color "#F6D55C"}
          :edges {:color {:color "#F6D55C"}}}
   :project {:nodes {:color "#3CAEA3"}
             :edges {:color {:color "#3CAEA3"}}}})


(def brick-hierarchical-layout-starting-levels
  {:project 0
   :base 1
   :component 2})
