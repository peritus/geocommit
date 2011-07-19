#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import subprocess
from os.path import curdir, abspath, join
from distutils.util import get_platform
from setuptools import setup
from setuptools.extension import Extension
from setuptools.command.easy_install import easy_install as _easy_install
from distutils.command.build_ext import build_ext as _build_ext
from distutils.command.install_data import install_data as _install_data
from distutils.version import StrictVersion

extra_args = {'cmdclass': {'install_data': _install_data}}

def runcmd(command, env=None):
    if env is None:
        env = {}
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=env)
    out, err = p.communicate()
    return out, err

if get_platform().startswith('macosx'):
    from os import chmod
    import stat

    # Remove PPC compilation if using Xcode 4.
    xc_path = "/usr/bin/xcodebuild"
    if os.path.exists(xc_path):
        version = runcmd([xc_path, '-version'])[0].splitlines()[0]
        if version.startswith('Xcode'):
            version_number = version.split()[1]
            if StrictVersion(version_number) >= StrictVersion('4.0'):
                os.environ['ARCHFLAGS'] = '-arch i386 -arch x86_64'

    class install_data(_install_data):
        '''
        On MacOS, the platform-specific lib dir is /System/Library/Framework/Python/.../
        which is wrong. Python 2.5 supplied with MacOS 10.5 has an Apple-specific fix
        for this in distutils.command.install_data#306. It fixes install_lib but not
        install_data, which is why we roll our own install_data class.
        '''
        def finalize_options(self):
            # By the time finalize_options is called, install.install_lib is set to the
            # fixed directory, so we set the installdir to install_lib. The
            # install_data class uses ('install_data', 'install_dir') instead.
            self.set_undefined_options('install', ('install_lib', 'install_dir'))
            _install_data.finalize_options(self)

    class easy_install(_easy_install):
        '''Need to properly set executable flags on geocommit binary.
        When installed with sudo python setup.py install these flags aren't
        set.
        '''
        def unpack_and_compile(self, egg_path, destination):
            _easy_install.unpack_and_compile(self, egg_path, destination)
            f = os.path.join(destination, "geocommit", "provider", "corelocation", "geocommit")
            mode = ((os.stat(f)[stat.ST_MODE]) | 0555) & 07755
            chmod(f, mode)

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

    extra_args['cmdclass'] = {
        'install_data': install_data,
        'build_ext': build_ext,
        'easy_install':easy_install
    }

    extra_args['ext_modules'] = [
        Extension(
          name = 'geocommit.provider.corelocation.geocommit',
          sources=['src/geocommit/provider/corelocation/geocommit.m', 'src/geocommit/provider/corelocation/GGCLDelegate.m'],
          extra_link_args=['-framework', 'Foundation', '-framework', 'CoreLocation']
        ),
    ]

    extra_args['zip_safe'] = False # because of the above binary

setup(
    name="geocommit",
    version='0.9.3beta1',
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
    data_files=[('share/man/man1', ['man/git-geo.1'])],
    **extra_args
    )
