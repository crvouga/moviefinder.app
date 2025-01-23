(ns moviefinder-app.home.frontend
  (:require [moviefinder-app.frontend.ui.bottom-buttons :as bottom-buttons]
            [moviefinder-app.frontend.screen :as screen]
            [moviefinder-app.frontend.ui.top-bar :as top-bar]
            [moviefinder-app.frontend.ui.icon :as icon]
            [moviefinder-app.frontend.store :as store]))


#_(def movies!
    (db-conn-sql/watch
     db-conn
     [:select [:media/title :media/year :media/genre-ids]
      :from :media
      :order-by [:media/popularity :desc]]))

(screen/reg!
 :screen/home
 (fn [i]
   [:div.w-full.flex-1.flex.flex-col
    [top-bar/view {:top-bar/title "Home"}]
    [:div.w-full.flex-1 "hello"]
    [bottom-buttons/view
     {:bottom-buttons/buttons
      [{:bottom-button/label "Home"
        :bottom-button/on-click #(store/put! i [:screen/clicked-link [:screen/home]])
        :bottom-button/view-icon icon/home}
       {:bottom-button/label "Profile"
        :bottom-button/on-click #(store/put! i [:screen/clicked-link [:screen/profile]])
        :bottom-button/view-icon icon/user-circle}]}]]))