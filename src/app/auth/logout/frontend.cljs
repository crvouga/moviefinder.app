(ns app.auth.logout.frontend
  (:require
   [app.frontend.mod :as mod]
   [clojure.core.async :as a]
   [lib.program :as p]
   [lib.result :as result]
   [lib.ui.button :as button]
   [lib.ui.drawer :as drawer]))



(defn- logic [i]
  (p/reg-reducer i ::set-request (fn [state [_ request]]  (assoc state ::request request)))
  (p/reg-reducer i ::set-opened (fn [state [_ opened]]  (assoc state ::opened opened)))

  (a/go-loop []
    (let [_ (a/<! (p/take! i ::clicked-logout-button))]
      #_(p/put! i [::set-request {:result/type :result/loading}])
      (p/put! i [::set-opened ::confirmation])

      #_(let [res (a/<! (p/eff! i [:rpc/send! [:rpc/logout]]))]
          (p/put! i [::set-request res])
          (p/put! i [:current-user/load]))

      (recur))))


(defn view-button [i]
  [:div
   [button/view
    {:button/on-click #(p/put! i [::clicked-logout-button])
     :button/loading? (-> i ::request result/loading?)
     :button/label "Logout"}]
   [drawer/view
    {:drawer/open? (-> i ::opened (= ::confirmation))
     :drawer/on-close #(p/put! i [::set-opened nil])}
    [:div.p-6.flex.items-center.justify-center "Are you sure you want to logout?"]]])

(mod/reg {:mod/name :mod/logout
          :mod/logic-fn logic})


