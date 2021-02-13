(ns poly-viz.vis-network.options)


(def default-vis-options
  {:layout {:hierarchical {:enabled true
                           :levelSeparation 50
                           :nodeSpacing 250}}
   :edges {:arrows {:to {:enabled true
                         :type "arrow"
                         :scaleFactor 0.8}}
           :smooth true}

   :nodes {:shape "box"
           :margin {:top 10 :bottom 10 :left 20 :right 20}}
   })


(def default-brick-vis-options
  {:component {:nodes {:color "#ED553B"}
               :edges {}}
   :base {:nodes {:color "#F6D55C"}
          :edges {}}
   :project {:nodes {:color "#3CAEA3"}
             :edges {}}})


(def brick-hierarchical-layout-starting-levels
  {:project 0
   :base 1
   :component 3})
