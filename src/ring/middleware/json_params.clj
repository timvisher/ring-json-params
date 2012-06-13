(ns ring.middleware.json-params
  (:require [clj-json.core :as json]))

(defn- json-request?
  [req]
  (if-let [#^String type (:content-type req)]
    (not (empty? (re-find #"^application/(vnd.+)?json" type)))))

(defn wrap-json-params [handler]
  (fn [req]
    (if-let [body (and (json-request? req) (:body req))]
      (let [bstr (slurp body)
            req* (assoc req :body bstr)
            json-params (try (json/parse-string bstr) (catch Exception e))
            req* (assoc req*
                   :json-params json-params
                   :params (merge (:params req) json-params))]
        (handler req*))
      (handler req))))
