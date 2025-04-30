(ns app.profile.frontend
  (:require
   [app.auth.current-user.frontend :as current-user]
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [app.profile.login-cta :as login-cta]
   [lib.ui.spinner-screen :as spinner-screen]
   [lib.ui.top-bar :as top-bar]
   [lib.program :as p]))


(defn- logic [i]
  (p/take-every!
   i :screen/screen-changed
   (fn [[_ [screen-name _]]]
     (when (= screen-name :screen/profile)
       (p/put! i [:current-user/load])))))

(defn view [i]
  [screen/view-screen i :screen/profile
   [top-bar/view {:top-bar/title "Profile"}]
   [:div.w-full.flex-1.flex.flex-col
    (cond
      (current-user/logged-out? i) [login-cta/view i]
      (current-user/logged-in? i) [:div "You are logged in."]
      :else [spinner-screen/view])]
   [top-level-bottom-buttons/view i]])

(mod/reg
 {:mod/name :mod/profile
  :mod/view-fn view
  :mod/logic-fn logic})