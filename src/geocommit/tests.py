import doctest
import unittest

def test_suite():
    return unittest.TestSuite([
      doctest.DocTestSuite('geocommit'),
      doctest.DocTestSuite('geocommit.provider.corelocation'),
      doctest.DocTestSuite('geocommit.locationprovider'),
      doctest.DocTestSuite('geocommit.networkmanager'),
      doctest.DocTestSuite('geocommit.wifilocationprovider'),
    ])
