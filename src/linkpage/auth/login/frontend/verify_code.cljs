(ns linkpage.auth.login.frontend.verify-code
  (:require
   [linkpage.frontend.routing :as routing]
   [linkpage.frontend.store :as store]
   [linkpage.core.result :as result]
   [linkpage.frontend.ui.button :as button]
   [linkpage.auth.login.frontend.shared :refer [view-layout]]
   [linkpage.frontend.ui.text-field :as text-field]))

(defmulti transition store/msg-type)

(defmethod transition :default [i] i)

(defmethod transition :store/initialized [i]
  (-> i
      (assoc-in [:store/state ::request] [:result/not-asked])))

(defmethod transition ::inputted-code [i]
  (-> i
      (assoc-in [:store/state ::code] (store/msg-payload i))))

(defn verify-code-eff [i]
  [:rpc/send! {:rpc/req [:login-rpc/verify-code {:user/phone-number (-> i routing/route-payload :user/phone-number)
                                                 :verify-sms/code (-> i :store/state ::code)}]
               :rpc/res #(vector ::verified-code %)}])

(defmethod transition ::submitted-verify-code-form [i]
  (-> i
      (update-in [:store/state] assoc ::request [:result/loading])
      (update-in [:store/effs] conj (verify-code-eff i))))

(defmethod transition ::verified-code [i]
  (let [payload (store/msg-payload i)
        msgs (when (result/ok? payload)
               [[:login/authenticated]])]
    (-> i
        (update-in [:store/state] assoc ::request payload)
        (update-in [:store/msgs] concat msgs))))

(defn loading? [i]
  (-> i :store/state ::request first (= :result/loading)))


(defmethod routing/view :route/login-verify-code [i]
  [view-layout "Verify Code"
   [:form
    {:on-submit #(do (.preventDefault %) (store/put! i [::submitted-verify-code-form]))}
    [:p "Enter the code we sent to " [:strong (-> i routing/route-payload :user/phone-number)]]
    [text-field/view
     {:text-field/label "Code"
      :text-field/value (-> i :store/state ::code)
      :text-field/required? true
      :text-field/disabled? (loading? i)
      :text-field/on-change #(store/put! i [::inputted-code %])}]
    [button/view
     {:button/type :button-type/submit
      :button/loading? (loading? i)
      :button/label "Verify Code"}]]])

(store/register-transition! transition)

