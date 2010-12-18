#!/usr/bin/env python
# -*- coding: utf-8 -*-

from os.path import curdir, abspath, join
from distutils.util import get_platform
from setuptools import setup
from setuptools.extension import Extension

ext_modules = []

if get_platform().startswith('macosx'):
    ext_modules += [
        Extension(
          name='_geocommit_corelocationprovider',
          sources=['src/geogit/corelocationprovider/geocommit.m', 'src/geogit/corelocationprovider/geocommitdelegate.m'],
          extra_link_args=['-framework', 'Foundation', '-framework', 'CoreLocation']
        ),
    ]

setup(
    name="geogit",
    ext_modules=ext_modules,
    version='0.1dev0',
    description="Geolocation for git",
    author="Nils Adermann <naderman+geogit@naderman.de>, Filip Noetzel <filip+geogit@j03.de>",
    author_email="filip+geogit@j03.de",
    license="Beerware",
    url="http://pypi.python.org/geogit",
    keywords="git geo",
    packages=['geogit'],
    package_dir={'': 'src'},
    entry_points = {'console_scripts': ['geogit = geogit:main']},
    )
