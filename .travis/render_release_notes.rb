#!/usr/bin/env ruby

# Renders the release notes for Github releases,
# and prints them to standard output

# Doesn't trim the header, which is done in shell

# Args:
# ARGV[0] : location of the file to render

require "liquid"
require "safe_yaml"

# include some custom liquid extensions
require_relative "../docs/_plugins/rule_tag"
require_relative "../docs/_plugins/custom_filters"

# START OF THE SCRIPT

unless ARGV.length == 1 && File.exists?(ARGV[0])
  print "\e[31m[ERROR] In #{$0}: The first arg must be a valid file name\e[0m"
  exit 1
end

release_notes_file = ARGV[0]

liquid_env = {
    # wrap the config under a "site." namespace because that's how jekyll does it
    'site' => YAML.load_file("docs/_config.yml"),
    # This signals the links in {% rule %} tags that they should be rendered as absolute
    'is_release_notes_processor' => true
}


to_render = File.read(release_notes_file)
rendered = Liquid::Template.parse(to_render).render(liquid_env)


print(rendered)
