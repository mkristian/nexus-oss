require 'nexus/dependency_helper_impl'
require 'minitest/spec'
require 'minitest/autorun'
require 'stringio'

describe Nexus::DependencyHelperImpl do

  let( :a ) { [ {:name=>"jbundler", :number=>"0.5.5", :platform=>"ruby", :dependencies=>[["bundler", "~> 1.5"], ["ruby-maven", "< 3.1.2, >= 3.1.1.0.1"]]}, {:name=>"jbundler", :number=>"0.5.4", :platform=>"ruby", :dependencies=>[["bundler", "~> 1.2"], ["ruby-maven", "< 3.1.2, >= 3.1.1.0.1"]]}, {:name=>"jbundler", :number=>"0.5.3", :platform=>"ruby", :dependencies=>[["bundler", "~> 1.2"], ["ruby-maven", "< 3.1.1, >= 3.1.0.0.1"]]} ] }
  
  let( :aa ) { [ {:name=>"jbundler", :number=>"0.5.5", :platform=>"ruby", :dependencies=>[["bundler", "~> 1.5"]] }] }

  let( :b ) { [ {:name=>"bundler", :number=>"1.6.0.rc2", :platform=>"ruby", :dependencies=>[]}, {:name=>"bundler", :number=>"1.6.0.rc", :platform=>"ruby", :dependencies=>[]} ] }

  subject { Nexus::DependencyHelperImpl.new }

  it 'should merge dependencies' do
    subject.add( subject.marshal_dump( b ) )
    subject.add( subject.marshal_dump( a ) )

    begin
      is = subject.input_stream( false )
      subject.marshal_load( is ).must_equal b + a
    ensure
      is.close if is
    end
    subject.gemnames.must_equal ["bundler", "jbundler"]
    subject.marshal_load( subject.input_stream_of( "bundler" ) ).must_equal b
    subject.marshal_load( subject.input_stream_of( "jbundler" ) ).must_equal a
  end

  it 'should merge dependencies with duplicates' do
    subject.add( subject.marshal_dump( b ) )
    subject.add( subject.marshal_dump( aa ) )
    subject.add( subject.marshal_dump( a ) )

    begin
      is = subject.input_stream( false )
      subject.marshal_load( is ).must_equal b + aa + a
    ensure
      is.close if is
    end
    subject.gemnames.must_equal ["bundler", "jbundler"]
    subject.marshal_load( subject.input_stream_of( "bundler" ) ).must_equal b
    subject.marshal_load( subject.input_stream_of( "jbundler" ) ).must_equal aa + a
  end

  it 'should merge dependencies without duplicates' do
    subject.add( subject.marshal_dump( b ) )
    subject.add( subject.marshal_dump( a ) )
    subject.add( subject.marshal_dump( aa ) )

    begin
      is = subject.input_stream( true )
      subject.marshal_load( is ).must_equal b + a
    ensure
      is.close if is
    end
    subject.gemnames.must_equal ["bundler", "jbundler"]
    subject.marshal_load( subject.input_stream_of( "bundler" ) ).must_equal b
    # duplicates are not eliminated here
    subject.marshal_load( subject.input_stream_of( "jbundler" ) ).must_equal a + aa
  end

  it 'should merge dependencies from gemspec.rz files' do
    dir = File.join( File.dirname( __FILE__ ), '../repo/quick/Marshal.4.8/h' )
    
    Dir[ File.join( dir, '*rz' ) ].each do |f|
      subject.add_gemspec( f )
    end

    begin
      is = subject.input_stream( false )
      subject.marshal_load( is ).must_equal [{:name=>"hufflepuf", :number=>"0.2.0", :platform=>"universal-java-1.5", :dependencies=>[]}, {:name=>"hufflepuf", :number=>"0.2.0", :platform=>"x86-mswin32-60", :dependencies=>[]}, {:name=>"hufflepuf", :number=>"0.1.0", :platform=>"ruby", :dependencies=>[]}, {:name=>"hufflepuf", :number=>"0.1.0", :platform=>"universal-java-1.5", :dependencies=>[]}]
    ensure
      is.close if is
    end
    subject.gemnames.must_equal ["hufflepuf"]
  end
end
    
