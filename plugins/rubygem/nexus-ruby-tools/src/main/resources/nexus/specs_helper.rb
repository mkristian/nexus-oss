#
# Sonatype Nexus (TM) Open Source Version
# Copyright (c) 2007-2014 Sonatype, Inc.
# All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
#
# This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
# which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
#
# Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
# of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
# Eclipse Foundation. All other trademarks are the property of their respective owners.
#
require 'nexus/rubygems_helper'

java_import org.sonatype.nexus.ruby.SpecsHelper

# this class can collect dependency data (which are an array of hashes).
# the collected data can be added as either marshaled stream of dependency
# data or marshaled stream of rzipped gemspec files.
#
# the basic idea is either to merge those dependency data and then
# marshal them again. or to have an array of dependecy data and
# split or group with their gemname as criterium.
#
# @author Christian Meier
module Nexus
  class SpecsHelperImpl
    include SpecsHelper
    include RubygemsHelper

    def initialize
      @result = []
    end

    def empty_specs
      dump_specs( [] )
    end
    
    def add( source )
      sources.each do |s|
        @result += load_specs( s )
      end
    end

    def input_stream( latest )
      @result.freeze
      if latest
        dump_specs( regenerate_latest( @result ) )
      else
        dump_specs( @result )
      end
    end

    def add_spec( spec, source, type )
      case type.downcase.to_sym
      when :latest
        do_add_spec( spec, source, true )
      when :release
        do_add_spec( spec, source ) unless spec.version.prerelease?
      when :prerelease
        do_add_spec( spec, source ) if spec.version.prerelease?
      end
    end

    def delete_spec( spec, source, type )
      specs = load_specs( source )
      old_entry = [ spec.name, spec.version, spec.platform.to_s ]
      if specs.member? old_entry
        specs.delete old_entry
        case type.downcase.to_sym
        when :latest
          if @releases
            # assume @releases is up to date
            specs = regenerate_latest( @releases )
            @releases = nil
          end
        when :release
          @release = specs
        end
        dump_specs( specs )
      end
    end

    # string representation with internal data
    # @return [String]
    def to_s
      @result.inspect
    end

    private

    def load_specs( io )
      marshal_load( io )
    end

    def dump_specs( specs )
      specs.uniq!
      specs.sort!
      marshal_dump( compact_specs( specs ) )
    end

    def compact_specs( specs )
      names = {}
      versions = {}
      platforms = {}

      specs.map do |( name, version, platform )|
        names[ name ] = name unless names.include? name
        versions[ version ] = version unless versions.include? version
        platforms[ platform ] = platform unless platforms.include? platform

        [ names[ name ], versions[ version ], platforms[ platform ] ]
      end
    end

    def do_add_spec( spec, source, latest = false )
      specs = load_specs( source )
      new_entry = [ spec.name, spec.version, spec.platform.to_s ]
      unless specs.member?( new_entry )
        if latest
          new_specs = regenerate_latest( specs + [ new_entry ] )
          dump_specs( new_specs ) if new_specs != specs
        else
          specs << new_entry
          dump_specs( specs )
        end
      end
    end

    def regenerate_latest( specs )
      specs.sort!
      specs.uniq!
      map = {}
      specs.each do |s|
        list = map[ s[ 0 ] ] ||= []
        list << s
      end
      result = []
      map.each do |name, list|
        list.sort!
        list.uniq!
        lastest_versions = {}
        list.each do |i|
          version = i[1]
          platform = i[2]
          lastest_versions[ platform ] = i
        end
        result += lastest_versions.collect { |k, v| v }
      end
      result
    end
  end
end
