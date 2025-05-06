(ns app.frontend.ui.top-level-bottom-buttons
  (:require [lib.ui.icon :as icon]
            [lib.ui.bottom-buttons :as bottom-buttons]
            [app.frontend.screen :as screen]
            [lib.program :as p]))


(defn view [i]
  [:div.w-full
   [bottom-buttons/view
    {:bottom-buttons/buttons
     [{:bottom-button/label "Feed"
       :bottom-button/selected? (-> i screen/to-screen-name (= :screen/feed))
       :bottom-button/on-click #(do
                                  (p/put! i [:screen/clicked-link [:screen/feed]]))
       :bottom-button/view-icon icon/home}
      {:bottom-button/label "Profile"
       :bottom-button/selected? (-> i screen/to-screen-name (= :screen/profile))
       :bottom-button/on-click #(do
                                  (p/put! i [:screen/clicked-link [:screen/profile]]))
       :bottom-button/view-icon icon/user-circle}]}]])