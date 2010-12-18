#!/usr/bin/env python
# -*- coding: utf-8 -*-

from os.path import curdir, abspath, join
from distutils.util import get_platform
from setuptools import setup
from setuptools.extension import Extension
from distutils.command.build_ext import build_ext as _build_ext

extra_args = {}

if get_platform().startswith('macosx'):
    class build_ext(_build_ext):
        '''
        This thing is an evil monkey-patch HACK:

        Instead of building a shared object file (.so) out of the
        objective-c source code provided, it builds an executable.

        Communication with that executable is then via stdout
        '''
        def build_extension(self, ext):
            def _link_executable(objects, ext_filename, **kwargs):
                del kwargs['export_symbols']
                del kwargs['build_temp']
                ext_filename = ext_filename.rstrip('.so')

                self.compiler.link_executable(objects, ext_filename, **kwargs)

            self.compiler.link_shared_object = _link_executable
            _build_ext.build_extension(self, ext)

    extra_args['cmdclass'] = { 'build_ext': build_ext }

    extra_args['ext_modules'] = [
        Extension(
          name = 'geogit.corelocationprovider',
          sources=['src/geogit/corelocationprovider/geocommit.m', 'src/geogit/corelocationprovider/geocommitdelegate.m'],
          extra_link_args=['-framework', 'Foundation', '-framework', 'CoreLocation']
        ),
    ]

setup(
    name="geogit",
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
    **extra_args
    )
