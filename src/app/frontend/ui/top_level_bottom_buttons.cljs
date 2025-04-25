(ns app.frontend.ui.top-level-bottom-buttons
  (:require [core.ui.icon :as icon]
            [core.ui.bottom-buttons :as bottom-buttons]
            [app.frontend.screen :as screen]
            [core.program :as p]))


(defn view [i]
  [:div.w-full
   [bottom-buttons/view
    {:bottom-buttons/buttons
     [{:bottom-button/label "Home"
       :bottom-button/selected? (-> i screen/screen-name (= :screen/home))
       :bottom-button/on-click #(do
                                  (p/put! i [:screen/clicked-link [:screen/home]]))
       :bottom-button/view-icon icon/home}
      {:bottom-button/label "Profile"
       :bottom-button/selected? (-> i screen/screen-name (= :screen/profile))
       :bottom-button/on-click #(do
                                  (p/put! i [:screen/clicked-link [:screen/profile]]))
       :bottom-button/view-icon icon/user-circle}]}]])