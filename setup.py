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
          name = 'geocommit.provider.corelocation.geocommit',
          sources=['src/geocommit/provider/corelocation/geocommit.m', 'src/geocommit/provider/corelocation/geocommitdelegate.m'],
          extra_link_args=['-framework', 'Foundation', '-framework', 'CoreLocation']
        ),
    ]

    extra_args['zip_safe'] = False # because of the above binary

setup(
    name="geocommit",
    version='0.9.1',
    description="Geolocation for git",
    long_description=open('README.rst').read(),
    author="Nils Adermann <naderman+geocommit@naderman.de>, Filip Noetzel <filip+geocommit@j03.de>, David Soria Parra <dsp+geocommit@experimentalworks.net>",
    author_email="filip+geocommit@j03.de",
    license="Beerware",
    url="http://pypi.python.org/geocommit",
    keywords="git geo",
    packages=['geocommit', 'geocommit.provider', 'geocommit.provider.corelocation'],
    package_dir={'': 'src'},
    entry_points = {'console_scripts': [
      'git-geo= geocommit:git_geo',
     ]},
    extras_require = {'test': ['zope.testing']},
    data_files=[('share/man/man1/', ['man/git-geo.1'])],
    **extra_args
    )
