(ns moviefinder-app.frontend.ui.top-level-bottom-buttons
  (:require
   [moviefinder-app.frontend.store :as store]
   [moviefinder-app.core.ui.icon :as icon]
   [moviefinder-app.core.ui.bottom-buttons :as bottom-buttons]
   [moviefinder-app.frontend.screen :as screen]))


(defn view [i]
  [bottom-buttons/view
   {:bottom-buttons/buttons
    [{:bottom-button/label "Home"
      :bottom-button/selected? (-> i screen/screen-name (= :screen/home))
      :bottom-button/on-click #(store/put! i [:screen/clicked-link [:screen/home]])
      :bottom-button/view-icon icon/home}
     {:bottom-button/label "Profile"
      :bottom-button/selected? (-> i screen/screen-name (= :screen/profile))
      :bottom-button/on-click #(store/put! i [:screen/clicked-link [:screen/profile]])
      :bottom-button/view-icon icon/user-circle}]}])