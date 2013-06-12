(ns bugzilla.checker
  (:require [clj-http.client :as http])
  (:use slingshot.slingshot
        [clojure.data.json :as json]))

(defrecord Bug [id])

(defprotocol Buggable
  (bug [x]))

(extend-protocol Buggable
  String (bug [x] (Bug. x)) ;; eg "123456"
  Integer (bug [x] (Bug. x)) ;; eg 123456
  Long (bug [x] (Bug. x))
  Bug (bug [x] x))

(def req-id (atom 0))

(def ^:dynamic *url* "https://bugzilla.redhat.com/jsonrpc.cgi")
(def ^:dynamic *user* nil)
(def ^:dynamic *password*  nil)
(def ^:dynamic *open-statuses*
  #{"NEW"
    "ASSIGNED"})

(defn get-bug
  ([buggable] (get-bug *url* *user* *password* buggable))
  ([url user password buggable]
     (let [retval
           (-> (http/post
                *url* 
                {:content-type :json
                 :body (json/json-str
                        {:method "Bug.get_bugs"
                         :id (swap! req-id inc)
                         :params [{"Bugzilla_login" *user*
                                   "Bugzilla_password" *password*
                                   "ids" (-> buggable bug :id list)}]})}) 
               :body read-json)]
       (if (:error retval)
         (throw+ (assoc (:error retval) :type ::api-error))
         (-> retval :result :bugs first map->Bug)))))

(defn fixed?
  ([open-statuses bug]
     ((complement open-statuses)
      (:status bug)))
  ([bug]
     (fixed? *open-statuses* bug)))

(defn link [bug]
  (let [url (java.net.URL. *url*)]
    (format "<a href='%s://%s/show_bug.cgi?id=%3$d'>%3$d: %4$s</a>"
            (.getProtocol url)
            (.getHost url)
            (:id bug)
            (:summary bug))))

