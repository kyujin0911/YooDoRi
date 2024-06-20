import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset, random_split
from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics import accuracy_score
from LocationAnalyzer import LocationAnalyzer

class ForecastLSTMClassification(nn.Module):
    def __init__(self, class_num: int, input_dim: int, hidden_dim: int, layer_dim: int, output_dim: int, dropout_prob: float = 0.2):
        super(ForecastLSTMClassification, self).__init__()
        self.hidden_dim = hidden_dim
        self.layer_dim = layer_dim

        self.lstm = nn.LSTM(input_dim, hidden_dim, layer_dim, batch_first=True, dropout=dropout_prob)
        self.fc = nn.Linear(hidden_dim, output_dim)
        self.softmax = nn.Softmax(dim=1)

    def forward(self, x):
        h0 = torch.zeros(self.layer_dim, x.size(0), self.hidden_dim).to(x.device)
        c0 = torch.zeros(self.layer_dim, x.size(0), self.hidden_dim).to(x.device)

        out, _ = self.lstm(x, (h0, c0))
        out = self.fc(out[:, -1, :])
        out = self.softmax(out)
        return out

class LSTMModel:
    def __init__(self, class_num: int, random_seed: int = 1234):
        self.random_seed = random_seed
        self.class_num = class_num
        torch.manual_seed(random_seed)
        np.random.seed(random_seed)

    def reshape_dataset(self, df: pd.DataFrame) -> np.array:
        dataset = df.values.reshape(df.shape)
        return dataset

    def split_sequences(self, dataset: np.array, seq_len: int, steps: int, single_output: bool) -> tuple:
        X, y = [], []
        for i in range(len(dataset) - seq_len - steps + 1):
            idx_in = i + seq_len
            idx_out = idx_in + steps

            if idx_out > len(dataset):
                break

            seq_x = dataset[i:idx_in, :-1]
            seq_y = dataset[idx_in:idx_out, -1]

            X.append(seq_x)
            y.append(seq_y[0] if single_output else seq_y)

        X = np.array(X)
        y = np.array(y)
        return X, y

    def split_train_valid_dataset(self, df: pd.DataFrame, seq_len: int, steps: int, single_output: bool, validation_split: float = 0.2) -> tuple:
        dataset = self.reshape_dataset(df=df)
        X, y = self.split_sequences(dataset=dataset, seq_len=seq_len, steps=steps, single_output=single_output)

        dataset_size = len(X)
        train_size = int(dataset_size * (1-validation_split))
        valid_size = dataset_size - train_size

        X_train, y_train = torch.tensor(X[:train_size, :], dtype=torch.float32), torch.tensor(y[:train_size], dtype=torch.long)
        X_val, y_val = torch.tensor(X[train_size:, :], dtype=torch.float32), torch.tensor(y[train_size:], dtype=torch.long)

        train_dataset = TensorDataset(X_train, y_train)
        val_dataset = TensorDataset(X_val, y_val)

        return train_dataset, val_dataset

    def build_and_compile_lstm_model(self, seq_len: int, n_features: int, hidden_dim: int, layer_dim: int, dropout_prob: float = 0.2, learning_rate: float = 0.001):
        model = ForecastLSTMClassification(self.class_num, n_features, hidden_dim, layer_dim, self.class_num, dropout_prob)
        criterion = nn.CrossEntropyLoss()
        optimizer = optim.Adam(model.parameters(), lr=learning_rate)
        return model, criterion, optimizer

    def fit_lstm(self, df: pd.DataFrame, steps: int, hidden_dim: int, layer_dim: int, dropout_prob: float, seq_len: int, single_output: bool, epochs: int, batch_size: int, validation_split: float, learning_rate: float):
        train_dataset, val_dataset = self.split_train_valid_dataset(df=df, seq_len=seq_len, steps=steps, single_output=single_output, validation_split=validation_split)
        train_loader = DataLoader(dataset=train_dataset, batch_size=batch_size, shuffle=True)
        val_loader = DataLoader(dataset=val_dataset, batch_size=batch_size, shuffle=False)

        model, criterion, optimizer = self.build_and_compile_lstm_model(seq_len=seq_len, n_features=train_dataset[0][0].shape[1], hidden_dim=hidden_dim, layer_dim=layer_dim, dropout_prob=dropout_prob, learning_rate=learning_rate)

        model.train()
        for epoch in range(epochs):
            for X_batch, y_batch in train_loader:
                optimizer.zero_grad()
                outputs = model(X_batch)
                loss = criterion(outputs, y_batch.view(-1))
                loss.backward()
                optimizer.step()
            print(f'Epoch {epoch+1}/{epochs}, Loss: {loss.item()}')

        return model

    def forecast_validation_dataset(self, model, val_loader):
        model.eval()
        y_pred_list, y_val_list = [], []

        with torch.no_grad():
            for X_batch, y_batch in val_loader:
                outputs = model(X_batch)
                _, predicted = torch.max(outputs.data, 1)
                y_pred_list.extend(predicted.tolist())
                y_val_list.extend(y_batch.tolist())
        return pd.DataFrame({"y": y_val_list, "yhat": y_pred_list})

    def pred(self, df: pd.DataFrame, model, steps: int, seq_len: int, single_output: bool, batch_size: int):
        dataset = self.reshape_dataset(df=df)
        X_test, y_test = self.split_sequences(dataset=dataset, seq_len=seq_len, steps=steps, single_output=single_output)

        X_test_tensor = torch.tensor(X_test, dtype=torch.float32)
        y_test_tensor = torch.tensor(y_test, dtype=torch.long)

        test_loader = DataLoader(TensorDataset(X_test_tensor, y_test_tensor), batch_size=batch_size, shuffle=False)

        model.eval()
        y_pred_list = []
        y_test_list = []

        with torch.no_grad():
            for X_batch, y_batch in test_loader:
                outputs = model(X_batch)
                _, predicted = torch.max(outputs.data, 1)
                y_pred_list.extend(predicted.tolist())
                y_test_list.extend(y_batch.tolist())

        y_pred = np.array(y_pred_list)
        y_test = np.array(y_test_list)
        accuracy = accuracy_score(y_test, y_pred)
        return y_pred, accuracy

if __name__ == '__main__':
    la = LocationAnalyzer(r"C:\Users\sk002\Downloads\138362.csv")
    df, meaningful_df = la.run_analysis()

    test_idx = int(len(df) * 0.8)
    df_train = df.iloc[:test_idx]
    df_test = df.iloc[test_idx:]

    # 파라미터 설정
    seq_len = 30
    steps = 30
    single_output = True
    lstm_params = {
        "seq_len": seq_len,
        "epochs": 30,
        "patience": 30,
        "learning_rate": 0.03,
        "hidden_dim": 64,
        "layer_dim": 2,
        "dropout_prob": 0,
        "batch_size": 32,
        "validation_split": 0.3,
    }

    lstm_model = LSTMModel(class_num=len(df['y'].unique()))
    trained_model = lstm_model.fit_lstm(
        df=df_train,
        steps=steps,
        hidden_dim=lstm_params["hidden_dim"],
        layer_dim=lstm_params["layer_dim"],
        dropout_prob=lstm_params["dropout_prob"],
        seq_len=seq_len,
        single_output=single_output,
        epochs=lstm_params["epochs"],
        batch_size=lstm_params["batch_size"],
        validation_split=lstm_params["validation_split"],
        learning_rate=lstm_params["learning_rate"]
    )

    y_pred, acc = lstm_model.pred(df=df_test, model=trained_model, steps=steps, seq_len=seq_len, single_output=single_output, batch_size=lstm_params["batch_size"])

    print(y_pred)
    print(f"acc : {acc}")