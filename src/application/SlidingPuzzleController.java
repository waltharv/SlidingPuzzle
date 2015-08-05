// Sliding Puzzle game
// Copyright (C) 2015  Walter Harvey
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.


package application;
 

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class SlidingPuzzleController {
	private int n = 4;
	private int size = n * n;
	private int moveCounter = 0;
	private TextField nametxt;
	private File scoresFile;
	private ArrayList<Score> highScores = new ArrayList<Score>();

	@FXML
	Slider resizeChoice;

	@FXML
	Label lblMoves;

	@FXML
	GridPane gridPane;

	@FXML
	public void TileClicked(ActionEvent e) {
		Button clicked = (Button) e.getSource();
		int clickedX = GridPane.getRowIndex(clicked);
		int clickedY = GridPane.getColumnIndex(clicked);

		Point[] adjacents = { new Point(clickedX - 1, clickedY),
				new Point(clickedX + 1, clickedY),
				new Point(clickedX, clickedY - 1),
				new Point(clickedX, clickedY + 1) };

		for (int i = 0; i < adjacents.length; i++) {
			Point p = adjacents[i];
			if (p.x < 0 || p.x > n - 1 || p.y < 0 || p.y > n - 1) {
				continue;
			}
			int index = (p.x * n) + p.y;
			Button adjacent = (Button) gridPane.getChildren().get(index);
			if (adjacent.getText() == "") {
				IncrementMove();
				MoveTile(clicked, adjacent);
				CheckForWin();

				break;
			}
			System.out.println("Adjacent tile: " + adjacent.getText());

		}

	}

	@FXML
	private void ChangeSizeHandler() {
		double choice = resizeChoice.getValue();
		int newN = (int) choice;
		this.n = newN;
		this.size = newN * newN;
		this.moveCounter = 0;

		ObservableList<Node> childs = gridPane.getChildren();
		int many = childs.size();
		for (int i = 0; i < many; i++) {
			childs.remove(many - i - 1);
		}

		SetupBoard();

		Stage myStage = (Stage) gridPane.getScene().getWindow();
		myStage.setWidth(this.n * 100);
		myStage.setHeight(this.n * 100 + 100);
	}

	private void CheckForWin() {
		ObservableList<Node> tiles = gridPane.getChildren();
		for (int i = 0; i < tiles.size(); i++) {
			Button b = (Button) tiles.get(i);
			String numStr = b.getText();
			try {
				int num = Integer.parseInt(numStr);
				if (num != i + 1) {
					return;
				}
			} catch (Exception e) {
				if (i == size - 1) {
					// winner!
					System.out.println("We have a winner!");
					ShowHighScores();

				}
			}
		}

	}

	
	private void ShowHighScores() {
		Stage s = new Stage();
		BorderPane bp = new BorderPane();
		GridPane gp = new GridPane();
		Scene sc = new Scene(bp, 200, 400);
		s.setScene(sc);
		Label l = new Label();
		l.setText("High scores");
		bp.setTop(l);
		bp.setCenter(gp);
		s.show();
		int counter = 0;
		
		
		try (BufferedReader reader = new BufferedReader(new FileReader(scoresFile));){
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				Score aScore = new Score();
				
				String[] data = line.split(",");
				aScore.setName(data[0]);
				aScore.setScore(data[1]);
				aScore.setDifficulty(data[2]);
				
				highScores.add(aScore);

				Label namelbl = new Label();
				namelbl.setText(aScore.getName());
				Label scorelbl = new Label();
				scorelbl.setText(aScore.getScore());
				
				//TODO sort scores before displaying!
				if(Integer.parseInt(aScore.getDifficulty()) == resizeChoice.getValue()){
					gp.add(namelbl, 0, counter);
					gp.add(scorelbl, 1, counter++);
				}
			}
			
			
			
		} catch (IOException ioe) {
			 ioe.printStackTrace();
		}
		
		nametxt = new TextField();
		Button btn = new Button();
		gp.add(nametxt, 0, counter);
		gp.add(btn, 1, counter);

		btn.setText("Save");
		btn.setOnAction(e -> saveScores(e));
	}
	

	private void saveScores(ActionEvent e) {
		
		try (PrintWriter scoreWriter = new PrintWriter(new BufferedWriter(new FileWriter(scoresFile, true))); ){
			String name = nametxt.getText();
			String score = Integer.toString(moveCounter);
			String difficulty = Integer.toString(this.n);
			
			scoreWriter.println(name + "," + score + "," + difficulty);
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
		ChangeSizeHandler();
	}


	private void MoveTile(Button clicked, Button adjacent) {
		String text1 = clicked.getText();
		String text2 = adjacent.getText();

		clicked.setText(text2);
		clicked.setStyle("-fx-background-color: #FFFFFF; "
				+ "-fx-text-fill:#000000;"

		);

		adjacent.setText(text1);
		adjacent.setStyle("-fx-background-color: #000000; "
				+ "-fx-text-fill:#FFFFFF;");

	}

	private void IncrementMove() {
		lblMoves.setText(String.format("%1d Moves", ++moveCounter));
	}

	@FXML
	private void initialize() {
		resizeChoice.setSnapToTicks(true);
		resizeChoice.setMajorTickUnit(1);
		resizeChoice.setMinorTickCount(0);
		resizeChoice.setValue(this.n);
		SetupBoard();
		
		try {
			scoresFile = new File("scores.txt");
			
			//Creates a new, empty file if it does not exist.
			scoresFile.createNewFile();
		}catch(IOException ioe){
			System.out.println("Could not create scores file." + ioe.getMessage());
			
		}
		

	}

	private void SetupBoard() {
		int[] shuffled = ShuffleTiles();

		for (int i = 0; i < size; i++) {
			int j = shuffled[i];

			Button tile = new Button();
			tile.setStyle("-fx-background-color: #000000; "
					+ "-fx-text-fill:#FFFFFF;");
			tile.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

			if (j + 1 < size) {
				tile.setText(String.format("%d", j + 1));
			} else {
				tile.setText("");
				tile.setStyle("-fx-background-color: #FFFFFF; ");
			}

			tile.setOnAction(e -> TileClicked(e));
			tile.setMinHeight(100);
			tile.setMinWidth(100);
			int row = i / n;
			int column = i % n;
			gridPane.add(tile, column, row);
			gridPane.setHgap(1);
			gridPane.setVgap(1);

		}

	}

	private int[] ShuffleTiles() {
		Random rand = new Random();
		int toShuffle[] = new int[size];

		for (int i = 0; i < size; i++) {
			toShuffle[i] = i;
		}

		 for (int i = 0; i < size; i++) {
		 int num = rand.nextInt(size);
		 int temp = toShuffle[i];
		 toShuffle[i] = toShuffle[num];
		 toShuffle[num] = temp;
		 }

		return toShuffle;

	}
}
