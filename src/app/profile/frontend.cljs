(ns app.profile.frontend
  (:require
   [app.auth.current-user.frontend :as current-user]
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [app.profile.login-cta :as login-cta]
   [clojure.core.async :as a]
   [lib.program :as p]
   [lib.result :as result]
   [lib.ui.avatar :as avatar]
   [lib.ui.button :as button]
   [lib.ui.spinner-screen :as spinner-screen]
   [lib.ui.top-bar :as top-bar]))


(defn- logic [i]
  (a/go
    (p/put! i [:current-user/load]))

  (p/take-every!
   i :screen/screen-changed
   (fn [[_ [screen-name _]]]
     (when (= screen-name :screen/profile)
       (p/put! i [:current-user/load])))))


(defn view-logout-button [i]
  [button/view
   {:button/full? true
    :button/on-click #(p/put! i [:logout/logout])
    :button/loading? (-> i ::request result/loading?)
    :button/color :button/color-neutral
    :button/label "Logout"}])

(defn view-edit-button [i]
  [button/view
   {:button/full? true
    :button/on-click #(p/put! i [::clicked-edit-profile-button])
    :button/loading? (-> i ::request result/loading?)
    :button/color :button/color-neutral
    :button/label "Edit Profile"}])

(defmulti view-body current-user/to-status)

(defmethod view-body :current-user/loading []
  [spinner-screen/view])

(defmethod view-body :current-user/logged-out [i]
  [login-cta/view i])

(defmethod view-body :current-user/logged-in [i]
  [:div.w-full.h-full.p-6.flex.flex-col.items-center.gap-6
   [:div.w-full.flex.flex-col.gap-4.items-center
    [avatar/view {:avatar/size 100
                  :avatar/src (-> i current-user/to-current-user :user/avatar-url)
                  :avatar/alt "avatar for current user"}]
    [:p.text-2xl.font-bold (-> i current-user/to-current-user :user/name)]]
   [:div.w-full.flex.items-center.gap-6
    [view-edit-button i]
    [view-logout-button i]]])

(defn view [i]
  [screen/view-screen i :screen/profile
   [top-bar/view {:top-bar/title "Profile"}]
   [:div.w-full.flex-1.flex.flex-col
    (view-body i)]
   [top-level-bottom-buttons/view i]])

(mod/reg
 {:mod/name :mod/profile
  :mod/view-fn view
  :mod/logic-fn logic})