(ns moviefinder-app.profile.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [core.ui.top-bar :as top-bar]
   [moviefinder-app.profile.login-cta :as login-cta]
   [moviefinder-app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [moviefinder-app.auth.current-user.frontend :as current-user]))


(screen/register!
 :screen/profile
 (fn [i]
   [:div.w-full.flex-1.flex.flex-col
    [top-bar/view {:top-bar/title "Profile"}]
    [:div.w-full.flex-1.flex.flex-col
     (if (current-user/logged-out? i)
       [login-cta/view i]
       [:div "You are logged in."])]
    [top-level-bottom-buttons/view i]]))