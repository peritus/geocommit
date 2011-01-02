(ns geocommit.test.core
  (:use [geocommit.core] :reload-all)
  (:import geocommit.core.Commit)
  (:use [clojure.test]))

(deftest parse-geocommit-short
  (testing "Parse geocommit short format"
    (is (= (parse-geocommit "github.com/dsp/mled"
			    "a532ba2af"
			    "David Soria Parra"
			    "This is a test\ngeocommit(1.0): lat 2.23, long 3.14, src nmg;"
			    "geocommit(1.0): lat 2.23, long 3.14, src nmg;")
	   (Commit. "geocommit:github.com/dsp/mled:a532ba2af",
		    "github.com/dsp/mled",
		    "a532ba2af",
		    "This is a test\ngeocommit(1.0): lat 2.23, long 3.14, src nmg;"
		    "David Soria Parra",
		    2.23,
		    3.14,
		    nil, nil,
		    "nmg",
		    nil, nil,
		    "geocommit")))))

(deftest parse-geocommit-long
  (testing "Parse geocommit long format"
    (is (= (parse-geocommit "github.com/dsp/mled"
			    "a532ba2af"
			    "David Soria Parra"
			    "This is a test"
			    "geocommit (1.0)\nlat: 2.23\nlong: 3.14\nsrc: nmg\n")
	   (Commit. "geocommit:github.com/dsp/mled:a532ba2af",
		    "github.com/dsp/mled",
		    "a532ba2af",
		    "This is a test",
		    "David Soria Parra",
		    2.23,
		    3.14,
		    nil, nil,
		    "nmg",
		    nil, nil
		    "geocommit")))))

(deftest contains-all
  (is (= true (contains-all? {:a 2 :b 3 :c nil} :a :b)))
  (is (= true (contains-all? {:a 3 :b 4} :a :b)))
  (is (= false (contains-all? {:a 3 :b 5} :a :c)))
  (is (= false (contains-all? {:a 5 :b 1} :a :b :c))))