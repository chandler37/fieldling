#! /bin/sh

# diffreshen (restores old modification times for unchanged files).
# Copyright (C) 2004 David L. Chandler
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.


# one_file_diffreshen.sh: A subroutine of diffreshen that runs cmp on
# a single file (two revisions of it, that is) and touches the new
# revision to have the old revision's modification time unless there
# is a difference in the two files.

# Usage: $1 is the file, $2 is 'old' path normalized, $3 is 'new' path
# normalized.


# I'm using '+', not forward slash as a quote character because it's
# rarer for Unix paths to contain a plus sign than a forward slash.
# If your path does contain '+', then you're out of luck.  FIXME: Die
# nicely if that's the case.
newfile="$1"
old="$2"
new="$3"
oldfile=`echo "$1" | sed -e "s+${new}+${old}+"`
if test "$newfile" = "$oldfile"; then
    echo "Oops!  old file and new file are both '$newfile'.  sed must not support '+' as a quote character for 's//'."
    exit 1
fi

# FIXME: /foo/foo-old/bar and /foo/foo/bar give us trouble if
# 'diffreshen foo-old foo' is used.  So test for the existence of
# s/^$new/$old/ and abort if it exists.

# Both GNU cmp and Solaris cmp return 2 on trouble and 1 for a diff.
# Both are treated the same way here, so we don't care if $oldfile
# exists.
if cmp "$oldfile" "$newfile" 1>/dev/null 2>&1; then
    # FIXME: Find out which versions of touch support this and
    # document that as a dependence.
    touch -r "$oldfile" "$newfile"
    if test "$DFVERBOSE" = "true"; then
        echo "touching '$newfile' so that is has the same modification time as '$oldfile'"
    fi
fi
exit 0
