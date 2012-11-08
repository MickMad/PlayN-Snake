/**
 * Copyright (C) 2012 Michele Perla (the.mickmad@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package mick.core;

import static playn.core.PlayN.*;

import java.util.Iterator;
import java.util.LinkedList;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Game;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Key;
import playn.core.Keyboard;


public class Test implements Game {
	private enum Direction { UP, DOWN, LEFT, RIGHT };
	private enum GameState { SPLASH, IN_GAME, GAME_OVER };
	
	Canvas canvas;
	
	ImageLayer bgLayer;
	ImageLayer pointsLayer;
	ImageLayer textsLayer;
	GroupLayer playersLayer;
	
	CanvasImage bgImage;
	CanvasImage textsImage;
	CanvasImage pointsImage;

	LinkedList<Entity> players;
	private Direction lastDirection=Direction.LEFT;
	private Direction nextDirection=Direction.LEFT;
	private GameState gameState=GameState.SPLASH;
	private static float UPDATE_TIME = 50;
	private static int MONSTER_TIME = 5;
	private static int SCORE = 9;
	private float updateTime;
	private float monsterDuration;
	private int monsterTime;
	private int points;
	Entity edible;
	Entity monster;
	int width = 640;
	int height = 480;
  @Override
  public void init() {
	  graphics().setSize(width, height);    
	  //background layer
	  bgImage = graphics().createImage(width, height);
	  canvas = bgImage.canvas();
	  canvas.setFillColor(0xff87ceeb);
	  canvas.fillRect(0, 0, width, height);
	  bgLayer = graphics().createImageLayer(bgImage);

	  //points text layer
	  pointsImage = graphics().createImage(width, 50);
	  pointsLayer = graphics().createImageLayer(pointsImage);
	  pointsLayer.setScale(3f);
	  //misc. texts layer
	  textsImage = graphics().createImage(width, 120);
	  textsLayer = graphics().createImageLayer(textsImage);
	  textsLayer.setScale(3f);
	  textsLayer.setTranslation(10f, 80f);
	  
	  //add all my layers to the root layer
	  graphics().rootLayer().add(bgLayer);
	  graphics().rootLayer().add(pointsLayer);
	  graphics().rootLayer().add(textsLayer);
	  
	  //set up the keyboard listener
	  keyboard().setListener(new Keyboard.Adapter(){
		  @Override
		  public void onKeyDown(Keyboard.Event event){
			  if (event.key()==Key.UP||event.key()==Key.W) nextDirection=Direction.UP;
			  else if (event.key()==Key.DOWN||event.key()==Key.S) nextDirection=Direction.DOWN;
			  else if (event.key()==Key.LEFT||event.key()==Key.A) nextDirection=Direction.LEFT;
			  else if (event.key()==Key.RIGHT||event.key()==Key.D) nextDirection=Direction.RIGHT;
			  else if (event.key()==Key.ENTER) changeState();
		  }
	  });
	  initSplashScreen();
  }
  @Override
  public void paint(float alpha) {
	  // the background automatically paints itself...
	  // but the text doesn't
	  if (gameState==GameState.IN_GAME){
		  //if i'm actually playing then empty the points canvas then draw the current score
		  pointsImage.canvas().clear();
		  pointsImage.canvas().setFillColor(0xff000000);
		  pointsImage.canvas().drawText(Integer.toString(points), 10f, 20f);
	  }else if (gameState==GameState.GAME_OVER){
		  //if the game is over, draw the last score achieved
		  textsImage.canvas().clear();
		  textsImage.canvas().setFillColor(0xff000000);
		  textsImage.canvas().drawText("GAME OVER!", 10f, 20f);
		  textsImage.canvas().drawText("Press ENTER to restart game", 10f, 50f);
	  }else if (gameState==GameState.SPLASH){
		  //if the game just began then show the controls for the game
		  textsImage.canvas().clear();
		  textsImage.canvas().setFillColor(0xff000000);
		  textsImage.canvas().drawText("THE SNAKE GAME", 10f, 20f);
		  textsImage.canvas().drawText("WASD or ARROWS to move", 10f, 35f);
		  textsImage.canvas().drawText("ENTER to start game", 10f, 50f);
	  }
  }
  public void start(){
	  
	  //change the state of the game to IN_GAME
	  gameState=GameState.IN_GAME;
	  //empty the counters used to store the score
	  points=0;
	  //empty the counter used to spawn the big monster every MONSTER_TIME*SCORE points
	  monsterTime=0;
	  //empty the counter used to refresh the game logic every UPDATE_TIME milliseconds
	  updateTime=0;
	  
	  //set the default direction for the snake
	  nextDirection=Direction.LEFT;
	  lastDirection=Direction.LEFT;
	  
	  //create a new snake
	  players = new LinkedList<Entity>();
	  for (int i=0; i<10; i++){
		  players.add(new Entity(graphics().createImageLayer(assets().getImage("images/ball.png")),playersLayer,320+16*i,240));
	  }
	  //create a default edible in a random position and place it in the game field
	  float x = (int)((random()*width)/16)*16;
	  float y = (int)((random()*height)/16)*16;
	  edible = new Entity(graphics().createImageLayer(assets().getImage("images/star.png")),playersLayer,x,y);
	  //create a monster in a random position and set it invisible
	  x = (int)((random()*width)/32)*32;
	  y = (int)((random()*height)/32)*32;
	  monster = new Entity(graphics().createImageLayer(assets().getImage("images/monster.png")),playersLayer,x,y);
	  monster.image().setVisible(false);
	  //finally, hide the splash screen text
	  textsLayer.setVisible(false);
	  pointsLayer.setVisible(true);
  }
  @Override
  public void update(float delta) {
	  if (gameState==GameState.IN_GAME){
		  //add delta milliseconds to the updateTime counter
		  updateTime+=delta;
		  if (updateTime>=UPDATE_TIME){
			  //if we counted UPDATE_TIME milliseconds...
			  checkForInvalidDirection();//...
			  //reset the counter...
			  updateTime=0;
			  //and...
			  moveSnake();
			  
			  //check if the head's position is the same as the current position of the edible 
			  Entity head = players.getFirst();
			  if (head.getTranslationX()==edible.getTranslationX()&&head.getTranslationY()==edible.getTranslationY()&&edible.image().visible()){
				  //if the snake is actually eating the edible add some points...
				  points+=SCORE;
				  //increment the monsterTime counter...
				  monsterTime++;
				  growSnake();
				  //and...
				  spawnEdible();
			  }
			  //if it's time to spawn the monster (the snake ate 5 edibles, or the user made 54 points)
			  if (monsterTime>MONSTER_TIME){
				  //reset the counter...
				  monsterTime=0;
				  //set the lifetime of the monster...
				  monsterDuration=2000;
				  //and...
				  spawnMonster();
			  }
			  //if the monster is on screen
			  if (monster.image().visible()){
				  //if the snake is eating the monster check the snake's head is in the same area as the monster
				  if (head.getTranslationX()>=monster.getTranslationX()&&head.getTranslationX()<=monster.getTranslationX()+32
						  &&head.getTranslationY()>=monster.getTranslationY()&&head.getTranslationY()<=monster.getTranslationY()+32){
					  points+=SCORE*(monsterDuration/200);
					  //add points based on how fast the player ate the snake and...
					  growSnake();
					  //and...
					  spawnEdible();
				  }
				  //if the snake didn't eat the monster, decrement the life of the monster
				  monsterDuration-=delta;
				  if (monsterDuration<=0){
					  //if the monster is "dead" (it disappeared)...
					  spawnEdible();
				  }
			  }
			  //finally...
			  checkForDeath();
		  }
	  }
  }
  private void checkForInvalidDirection(){
	  //this method ensures that the head of the snake turns 90 degrees
	  if (!(lastDirection==Direction.DOWN&&nextDirection==Direction.UP)&&
			  !(lastDirection==Direction.UP&&nextDirection==Direction.DOWN)&&
			  !(lastDirection==Direction.LEFT&&nextDirection==Direction.RIGHT)&&
			  !(lastDirection==Direction.RIGHT&&nextDirection==Direction.LEFT)) lastDirection=nextDirection;
  }
  private void moveSnake(){
	  //this method takes the tail of the snakes and puts it as its head
	  //and eventually wraps it around when it crosses the borders of the game field
	  Entity head = players.getFirst();
	  Entity tail = players.removeLast();
	  float x=0, y=0;
	  if (lastDirection==Direction.UP){
		  x = head.getTranslationX();
		  y = head.getTranslationY()-16;
		  if (y<0) y+=height;
	  }else if (lastDirection==Direction.DOWN){
		  x = head.getTranslationX();
		  y = head.getTranslationY()+16;
		  if (y>=height) y=0;
	  }else if (lastDirection==Direction.LEFT){
		  x = head.getTranslationX()-16;
		  y = head.getTranslationY();
		  if (x<0) x+=width;
	  }else if (lastDirection==Direction.RIGHT){
		  x = head.getTranslationX()+16;
		  y = head.getTranslationY();
		  if (x>=width) x=0;
	  }
	  tail.setTranslation(x, y);
	  players.addFirst(tail);
  }
  private void growSnake(){
	  //this method adds a new head to the snake
	  Entity head = players.getFirst();
	  float x=0, y=0;
	  if (lastDirection==Direction.UP){
		  x = head.getTranslationX();
		  y = head.getTranslationY()-16;

	  }else if (lastDirection==Direction.DOWN){
		  x = head.getTranslationX();
		  y = head.getTranslationY()+16;

	  }else if (lastDirection==Direction.LEFT){
		  x = head.getTranslationX()-16;
		  y = head.getTranslationY();

	  }else if (lastDirection==Direction.RIGHT){
		  x = head.getTranslationX()+16;
		  y = head.getTranslationY();
	  }
	  Entity newHead = new Entity(graphics().createImageLayer(assets().getImage("images/ball.png")),playersLayer,x,y);
	  players.addFirst(newHead);
  }
  private void checkForDeath(){
	  //this method checks if the head of the snake intersects any of the other parts of the snake
	  //if it is, it stops the game
	  Iterator<Entity> iterator = players.iterator();
	  boolean dead = false;
	  Entity head = iterator.next();
	  while (iterator.hasNext()&&!dead){
		  Entity next = iterator.next();
		  if (next.getTranslationX()==head.getTranslationX()&&next.getTranslationY()==head.getTranslationY()) dead=true;
	  }
	  if (dead) {
		  gameState=GameState.GAME_OVER;
		  textsLayer.setVisible(true);
	  }
  }
  private void spawnEdible(){
	  //this method set the default edible in a random position and in the gamefield (visible)
	  float x=0, y=0;
	  x = (int)((random()*width)/16)*16;
	  y = (int)(((random()*(height-16))/16)+1)*16;
	  edible.setTranslation(x, y);
	  edible.image().setVisible(true);
	  monster.image().setVisible(false);
  }
  private void spawnMonster(){
	//this method set the monster in a random position and in the gamefield (visible)
	  float x=0, y=0;
	  x = (int)((random()*width)/16)*16;
	  y = (int)(((random()*(height-16))/16)+1)*16;
	  monster.setTranslation(x, y);
	  monster.image().setVisible(true);
	  edible.image().setVisible(false);
  }
  private void changeState(){
	  //if the user pressed enter on the splash screen, start the game
	  if (gameState==GameState.SPLASH) start();

	  //if the user pressed enter on the game over screen, set the game back to the splash screen
	  if (gameState==GameState.GAME_OVER) initSplashScreen();
  }
  private void initSplashScreen(){
	  //empty the layer containing the graphics for the snake and for the edibles, if needed
	  if (playersLayer!=null) playersLayer.destroy();
	  //create the layer used to contain the snake and the edibles images
	  playersLayer = graphics().createGroupLayer();
	  graphics().rootLayer().add(playersLayer);
	  //hide the points
	  pointsLayer.setVisible(false);
	  gameState=GameState.SPLASH;
  }
  @Override
  public int updateRate() {
    return 25;
  }
}
