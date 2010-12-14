#!/usr/bin/env python
# -*- coding: utf-8 -*-

from os.path import curdir, abspath, join
from distutils.util import get_platform
from setuptools import setup

install_requires = []
setup_requires = []

APP = ['src/geogit/corelocation.py']
DATA_FILES = []
OPTIONS = {
    'argv_emulation': False,
    'iconfile': join(abspath(curdir), 'src/geogit/geogit_logo.icns'),
}

if get_platform().startswith('macosx'):
    install_requires += ['pyobjc-core', 'pyobjc-framework-Cocoa', 'pyobjc-framework-CoreLocation']
    setup_requires += ['py2app']

setup(
    name="geogit",
    app=APP,
    data_files=DATA_FILES,
    options={'py2app': OPTIONS},
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
    setup_requires=setup_requires,
    )
