(ns moviefinder-app.profile.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]))


(screen/reg!
 :screen/profile
 (fn [i]
   [:div.w-full.flex-1.flex.flex-col
    [top-bar/view {:top-bar/title "Profile"}]
    [:div.w-full.flex-1 "Profile"]
    [top-level-bottom-buttons/view i]]))