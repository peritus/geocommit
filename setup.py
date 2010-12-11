#!/usr/bin/env python
# -*- coding: utf-8 -*-

from distutils.core import setup
from distutils.util import get_platform

install_requires = []

if get_platform().startswith('macosx'):
    install_requires += ['pyobjc-core', 'pyobjc-framework-Cocoa', 'pyobjc-framework-CoreLocation']

setup(
    name="geogit",
    version='0.1dev0',
    description="Geolocation for git",
    author="Nils Adermann <naderman@naderman.de>, Filip Noetzel <filip+geogit@j03.de>",
    author_email="filip+geogit@j03.de",
    license="Beerware",
    url="http://pypi.python.org/geogit",
    keywords="git geo",
    packages=['geogit'],
    package_dir={'': 'src'},
    entry_points = {'console_scripts': ['geogit = geogit:main']},
    install_requires=install_requires,
    )
