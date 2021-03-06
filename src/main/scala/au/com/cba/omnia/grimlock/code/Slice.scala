// Copyright 2014 Commonwealth Bank of Australia
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package grimlock

import grimlock.position._

/** Base trait that encapsulates dimension on which to operate. */
sealed trait Slice[P <: Position, D <: Dimension] {
  /**
   * Return type of [[Slice.selected]]; a position of dimension less than `P`.
   *
   * @note [[Slice.S]] and [[Slice.R]] together make `P`.
   */
  type S <: Position with ExpandablePosition

  /**
   * Return type of [[Slice.remainder]]; a position of dimension less than `P`.
   *
   * @note [[Slice.S]] and [[Slice.R]] together make `P`.
   */
  type R <: Position

  /** Return type of [[Slice.inverse]].  */
  type I <: Slice[P, D]

  val dimension: D

  /** Returns the selected coordinate(s) for the given `pos`. */
  def selected(pos: P): S
  /** Returns the remaining coordinate(s) for the given `pos`. */
  def remainder(pos: P): R
  /** Returns the inverse of this. */
  def inverse(): I
}

/**
 * Indicates that the selected coordinate is indexed by `dimension`. In
 * other words, when a groupBy is performed, it is performed using a
 * [[position.Position1D]] consisting of the coordinate at index `dimension`.
 *
 * @param dimension [[position.Dimension]] of the selected coordinate.
 */
case class Over[P <: Position with ReduceablePosition, D <: Dimension](dimension: D) extends Slice[P, D] {
  type S = Position1D
  type R = P#L
  type I = Along[P, D]

  def selected(pos: P): S = Position1D(pos.get(dimension))
  def remainder(pos: P): R = pos.remove(dimension)
  def inverse(): I = Along(dimension)
}

/**
 * Indicates that the selected coordinates are all except the one indexed
 * by `dimension`. In other words, when a groupBy is performed, it is
 * performed using a [[position.Position]] (type [[position.ReduceablePosition.L]])
 * consisting of all coordinates except that at index `dimension`.
 *
 * @param dimension [[position.Dimension]] of the coordinate to exclude.
 */
case class Along[P <: Position with ReduceablePosition, D <: Dimension](dimension: D) extends Slice[P, D] {
  type S = P#L
  type R = Position1D
  type I = Over[P, D]

  def selected(pos: P): S = pos.remove(dimension)
  def remainder(pos: P): R = Position1D(pos.get(dimension))
  def inverse(): I = Over(dimension)
}

